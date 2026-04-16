package com.arise.habitquest.worker

import android.content.pm.ApplicationInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.generator.MissionTemplates
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.GenerateDailyMissionsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class DebugSeedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var missionRepository: MissionRepository

    @Inject
    lateinit var generator: MissionGenerator

    @Inject
    lateinit var generateDailyMissionsUseCase: GenerateDailyMissionsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DEBUG_SEED_WEEK) return

        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggable) {
            Log.w(TAG, "Ignoring debug seed request in non-debug build.")
            return
        }

        runBlocking {
            val profile = userRepository.getUserProfile()
            if (profile == null || !profile.onboardingComplete) {
                Log.w(TAG, "Cannot seed week: onboarding/profile missing.")
                return@runBlocking
            }

            val today = LocalDate.now()
            val weekStart = today.with(DayOfWeek.MONDAY)
            var fallbackInserted = 0

            for (date in generateSequence(weekStart) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }) {
                generateDailyMissionsUseCase(profile, date)

                val dayMissions = missionRepository.getMissionsForDate(date)
                val hasPhysical = dayMissions.any { it.category == MissionCategory.PHYSICAL }
                if (hasPhysical) continue

                // Fallback for QA/dev: ensure physical load exists for the body-coverage chart.
                val fallbackTemplates = listOf(
                    MissionTemplates.pushUps.id,
                    MissionTemplates.squat.id,
                    MissionTemplates.morningRun.id,
                    MissionTemplates.plank.id,
                    MissionTemplates.stretching.id
                )

                val fallbackMissions = fallbackTemplates
                    .mapNotNull { templateId ->
                        generator.generateSingleDailyMission(
                            profile = profile,
                            templateId = templateId,
                            date = date,
                            shadowCompletions = 0
                        )
                    }
                    .take(3)

                if (fallbackMissions.isNotEmpty()) {
                    missionRepository.insertMissions(fallbackMissions)
                    fallbackInserted += fallbackMissions.size
                }
            }

            Log.i(TAG, "Seed complete for week. fallbackInserted=$fallbackInserted")
        }
    }

    companion object {
        const val ACTION_DEBUG_SEED_WEEK = "com.arise.habitquest.action.DEBUG_SEED_WEEK"
        private const val TAG = "DebugSeedReceiver"
    }
}
