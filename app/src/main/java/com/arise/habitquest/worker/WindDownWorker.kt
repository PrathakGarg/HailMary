package com.arise.habitquest.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Fires at 21:15 each evening to remind the user about evening-specific and
 * wellness missions (sleep, journaling, gratitude, stretching, etc.) that are
 * still incomplete.  Intentionally softer in tone than the generic 20:00
 * evening reminder — these tasks require intentional wind-down, not a panic.
 */
@HiltWorker
class WindDownWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            timeProvider.sync()
            val allMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())

            val eveningMissions = allMissions.filter { mission ->
                mission.isActive &&
                    !SleepReminderWorker.isSleepMission(mission.title) &&
                    (mission.scheduledTimeHint == "EVENING" ||
                        mission.category == MissionCategory.WELLNESS)
            }

            if (eveningMissions.isEmpty()) return Result.success()

        val profile = userRepository.getUserProfile()
        val hunterName = profile?.hunterName ?: "Hunter"

        val hasSleep = eveningMissions.any { it.title.contains("sleep", ignoreCase = true) ||
                it.title.contains("bed", ignoreCase = true) }
        val hasJournal = eveningMissions.any { it.title.contains("journal", ignoreCase = true) ||
                it.title.contains("reflect", ignoreCase = true) }
        val hasGratitude = eveningMissions.any { it.title.contains("gratitude", ignoreCase = true) }
        val hasMeditation = eveningMissions.any { it.title.contains("meditat", ignoreCase = true) ||
                it.title.contains("mind", ignoreCase = true) }

        val body = buildBody(hunterName, eveningMissions, hasSleep, hasJournal, hasGratitude, hasMeditation)

        NotificationHelper.send(
            context = appContext,
            channelId = "arise_wind_down",
            channelName = "Evening Wind-Down",
            channelDescription = "Evening ritual and wellness mission reminders.",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            notificationId = NOTIFICATION_ID,
            title = "[ EVENING PROTOCOL ]",
            body = body
        )
        Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }

    private fun buildBody(
        name: String,
        missions: List<Mission>,
        hasSleep: Boolean,
        hasJournal: Boolean,
        hasGratitude: Boolean,
        hasMeditation: Boolean
    ): String {
        return when {
            hasSleep -> "Hunter $name. Your sleep gate is still open. " +
                "The System monitors recovery — poor rest compounds into weakness. " +
                "Begin your bedtime ritual now."
            hasMeditation -> "$name. Your mind gate remains unsealed. " +
                "Stillness is not weakness — it is recovery. " +
                "${missions.size} evening ${if (missions.size == 1) "gate" else "gates"} await."
            hasJournal || hasGratitude -> "$name. The System requires your reflection. " +
                "${missions.size} evening ${if (missions.size == 1) "gate" else "gates"} remain. " +
                "Close the day with intention."
            else -> "Hunter $name. ${missions.size} evening " +
                "${if (missions.size == 1) "gate remains" else "gates remain"} unsealed. " +
                "The System does not allow unfinished business to carry into rest."
        }
    }

    companion object {
        const val WORK_NAME = "arise_wind_down"
        private const val NOTIFICATION_ID = 1003

        fun schedule(workManager: WorkManager, timeProvider: TimeProvider) {
            val now = timeProvider.trustedNow().toLocalTime()
            val target = LocalTime.of(21, 15)
            var delayMinutes = now.until(target, ChronoUnit.MINUTES)
            if (delayMinutes <= 0) delayMinutes += 24 * 60

            val request = PeriodicWorkRequestBuilder<WindDownWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
