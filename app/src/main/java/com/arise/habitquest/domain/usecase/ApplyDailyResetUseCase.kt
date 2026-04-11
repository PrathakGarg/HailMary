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
        val today = timeProvider.today()
        val yesterday = today.minusDays(1)
        val isRestDay = today.dayOfWeek == DayOfWeek.of(((profile.restDay % 7) + 1).coerceIn(1, 7))

        // Archive yesterday's missions into daily log
        val yesterdayMissions = missionRepository.getMissionsForDate(yesterday)
        val completed = yesterdayMissions.filter { it.isCompleted }
        val failed = yesterdayMissions.filter { it.isFailed }
        val skipped = yesterdayMissions.filter { it.isSkipped }
        val totalYesterday = yesterdayMissions.size
        val completionRate = if (totalYesterday > 0) completed.size.toFloat() / totalYesterday else 0f

        val xpGained = completed.sumOf { it.effectiveXpReward }
        val xpLost = failed.sumOf { it.penaltyXp }
        val hpLost = failed.sumOf { it.penaltyHp }
        val hpGained = completed.size * 5

        val systemMessage = generator.generateSystemMessage(completionRate, profile.streakCurrent, profile.rank)

        val log = DailyLogEntity(
            date = yesterday.toString(),
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
            totalMissions = totalYesterday,
            systemMessage = systemMessage,
            wasRestDay = isRestDay
        )
        dailyLogDao.upsertLog(log)

        // HP regeneration
        if (isRestDay) {
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

        // Generate today's missions (skip if rest day)
        if (!isRestDay) {
            generateDailyMissions(profile, today)
        }

        // Prune missions older than 30 days
        missionRepository.pruneOldDailyMissions(today.minusDays(30))

        // Record last reset date
        dataStore.setLastDailyResetDate(today.toString())
    }
}
