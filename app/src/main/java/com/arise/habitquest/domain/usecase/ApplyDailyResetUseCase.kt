package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.database.dao.DailyLogDao
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionPenaltyPolicy
import com.arise.habitquest.domain.usecase.policy.ProgressionStatePolicy
import com.arise.habitquest.domain.usecase.policy.RestDayPolicy
import com.arise.habitquest.domain.model.OnboardingConfigCodec
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
        val currentSessionIsRestDay = RestDayPolicy.isRestDay(sessionDate, profile.restDay)
        val expiringWasRestDay = RestDayPolicy.isRestDay(expiringDate, profile.restDay)

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
        val fullMissDay = !expiringWasRestDay && completed.isEmpty() && totalExpiring > 0
        val newMissDays = if (fullMissDay) profile.consecutiveMissDays + 1 else 0
        val isGraceDay = fullMissDay && newMissDays == 1 && !profile.pendingWarning

        val xpLost = if (expiringWasRestDay || isGraceDay) {
            0
        } else {
            failed.sumOf { MissionPenaltyPolicy.systemMandateXpPenalty(it) }
        }
        val hpLost = if (expiringWasRestDay || isGraceDay) {
            0
        } else {
            failed.sumOf { MissionPenaltyPolicy.systemMandateHpPenalty(it) }
        }
        val hpGained = completed.size * 5

        if (fullMissDay) {
            userRepository.updateMissState(newMissDays, isGraceDay)
        } else {
            userRepository.updateMissState(0, false)
        }

        if (xpLost > 0) {
            val nextXp = (profile.xp - xpLost).coerceAtLeast(0L)
            userRepository.updateXpAndLevel(nextXp, profile.level, profile.rank, profile.xpToNextLevel)
        }

        val hpAfterPenalty = if (hpLost > 0) {
            val nextHp = (profile.hp - hpLost).coerceAtLeast(0)
            userRepository.updateHp(nextHp)
            nextHp
        } else {
            profile.hp
        }

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
            userRepository.updateHp((hpAfterPenalty + regenHp).coerceAtMost(profile.maxHp))
        } else if (completionRate >= 0.6f) {
            val regenHp = (profile.maxHp * 0.10f).toInt()
            userRepository.updateHp((hpAfterPenalty + regenHp).coerceAtMost(profile.maxHp))
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

            val safetyThrottle = newDiff < currentDiff
            val nextState = ProgressionStatePolicy.nextState(
                current = profile.progressionState,
                recentCompletionRate = avgRate,
                consecutiveMissDays = newMissDays,
                safetyThrottle = safetyThrottle
            )
            val onboardingConfig = OnboardingConfigCodec.decode(profile.onboardingAnswersJson)
            val recommendation = ProgressionStatePolicy.recommendTransition(
                goalCategories = onboardingConfig.goalCategories,
                recentCompletionRate = avgRate,
                consecutiveMissDays = newMissDays
            )
            userRepository.updateProgressionState(nextState.name, recommendation)
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
