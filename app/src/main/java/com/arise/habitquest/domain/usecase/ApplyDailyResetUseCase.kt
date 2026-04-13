package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.database.dao.DailyLogDao
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class ApplyDailyResetUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val dailyLogDao: DailyLogDao,
    private val generateDailyMissions: GenerateDailyMissionsUseCase,
    private val generator: MissionGenerator,
    private val dataStore: OnboardingDataStore,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke() {
        val profile = userRepository.getUserProfile() ?: return
        val sessionDate = timeProvider.sessionDay()
        val expiringDate = sessionDate.minusDays(1)
        val currentSessionIsRestDay = sessionDate.dayOfWeek == DayOfWeek.of(((profile.restDay % 7) + 1).coerceIn(1, 7))
        val expiringWasRestDay = expiringDate.dayOfWeek == DayOfWeek.of(((profile.restDay % 7) + 1).coerceIn(1, 7))

        // Any active daily gate from the expiring session date becomes failed at rollover.
        missionRepository.failActiveDailyMissionsForDate(expiringDate)

        // Archive expiring day's missions into daily log
        val expiringMissions = missionRepository.getMissionsForDate(expiringDate)
        val completed = expiringMissions.filter { it.isCompleted }
        val failed = expiringMissions.filter { it.isFailed }
        val skipped = expiringMissions.filter { it.isSkipped }
        val totalExpiring = expiringMissions.size
        val completionRate = if (totalExpiring > 0) completed.size.toFloat() / totalExpiring else 0f

        val xpGained = completed.sumOf { it.effectiveXpReward }
        val xpLost = failed.sumOf { it.penaltyXp }
        val hpLost = failed.sumOf { it.penaltyHp }
        val hpGained = completed.size * 5

        val systemMessage = generator.generateSystemMessage(completionRate, profile.streakCurrent, profile.rank)

        val log = DailyLogEntity(
            date = expiringDate.toString(),
            completedIdsJson = completed.map { it.id }.toString(),
            failedIdsJson = failed.map { it.id }.toString(),
            skippedIdsJson = skipped.map { it.id }.toString(),
            xpGained = xpGained,
            xpLost = xpLost,
            hpLost = hpLost,
            hpGained = hpGained,
            rankSnapshot = profile.rank.name,
            levelSnapshot = profile.level,
            completionRate = completionRate,
            totalMissions = totalExpiring,
            systemMessage = systemMessage,
            wasRestDay = expiringWasRestDay
        )
        dailyLogDao.upsertLog(log)

        // Break day streak only when a non-rest day had zero completed missions.
        if (!expiringWasRestDay && completed.isEmpty()) {
            userRepository.updateStreak(0, profile.streakBest)
        }

        // HP regeneration
        if (currentSessionIsRestDay) {
            val regenHp = (profile.maxHp * 0.30f).toInt()
            userRepository.updateHp((profile.hp + regenHp).coerceAtMost(profile.maxHp))
        } else if (completionRate >= 0.6f) {
            val regenHp = (profile.maxHp * 0.10f).toInt()
            userRepository.updateHp((profile.hp + regenHp).coerceAtMost(profile.maxHp))
        }

        // Adaptive difficulty update (weekly)
        val recentLogs = dailyLogDao.getRecentLogs(7)
        if (recentLogs.size >= 7) {
            val avgRate = recentLogs.map { it.completionRate }.average().toFloat()
            val currentDiff = profile.adaptiveDifficulty
            val newDiff = when {
                avgRate < 0.45f -> (currentDiff * 0.85f).coerceAtLeast(0.4f)
                avgRate > 0.85f -> (currentDiff * 1.10f).coerceAtMost(2.0f)
                else -> currentDiff
            }
            if (newDiff != currentDiff) {
                userRepository.updateAdaptiveDifficulty(newDiff)
            }
        }

        // Increment day count
        userRepository.incrementDayCount()

        // Generate the active session day's missions (skip if rest day)
        if (!currentSessionIsRestDay) {
            generateDailyMissions(profile, sessionDate)
        }

        // Prune missions older than 30 days
        missionRepository.pruneOldDailyMissions(sessionDate.minusDays(30))

        // Record last reset date
        dataStore.setLastDailyResetDate(sessionDate.toString())
    }
}
