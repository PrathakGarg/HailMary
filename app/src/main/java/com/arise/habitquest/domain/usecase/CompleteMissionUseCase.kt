package com.arise.habitquest.domain.usecase

import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionPenaltyPolicy
import javax.inject.Inject

data class CompletionResult(
    val xpGained: Int,
    val hpRestored: Int,
    val statGains: Map<String, Int>,
    val levelUpResult: LevelUpResult?,
    val newAchievements: List<Achievement>,
    val promotedToShadow: Boolean
)

class CompleteMissionUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val dataStore: OnboardingDataStore,
    private val timeProvider: TimeProvider,
    private val checkLevelUp: CheckLevelUpUseCase,
    private val unlockAchievement: UnlockAchievementUseCase
) {
    suspend operator fun invoke(
        mission: Mission,
        profile: UserProfile,
        useMiniVersion: Boolean = false
    ): CompletionResult {
        val effectiveXp = if (useMiniVersion) (mission.xpReward * 0.5f).toInt() else mission.xpReward
        val newStreak = mission.streakCount + 1
        val promotedToShadow = newStreak == 21

        // Mark mission complete in DB
        missionRepository.markCompleted(mission.id, newStreak, useMiniVersion)

        // Update XP
        val newXp = profile.xp + effectiveXp
        userRepository.updateXpAndLevel(newXp, profile.level, profile.rank, profile.xpToNextLevel)

        // Restore HP: +5 per mission, capped at maxHp
        val hpRestored = 5
        val newHp = (profile.hp + hpRestored).coerceAtMost(profile.maxHp)
        userRepository.updateHp(newHp)

        // Update stats
        val newStats = profile.stats.add(mission.statRewards)
        userRepository.updateStats(newStats)

        // Update streak once per day: only the first completed mission for the
        // active session day should advance the day-streak counter.
        val completedToday = missionRepository.countCompletedForDate(timeProvider.sessionDay())
        val streakCurrentAfterCompletion = if (completedToday == 1) {
            profile.streakCurrent + 1
        } else {
            profile.streakCurrent
        }
        val newStreakBest = maxOf(profile.streakBest, streakCurrentAfterCompletion)
        userRepository.updateStreak(streakCurrentAfterCompletion, newStreakBest)

        // Increment counters
        userRepository.incrementMissionStats(effectiveXp.toLong())

        // Reset miss state (completing a mission clears the warning)
        userRepository.updateMissState(0, false)

        // Check achievements first — they may award bonus XP that should count
        // toward the level-up check below.
        val updatedProfile = profile.copy(xp = newXp, stats = newStats)
        val finalProfile = updatedProfile.copy(
            streakCurrent = streakCurrentAfterCompletion,
            streakBest = newStreakBest,
            totalMissionsCompleted = profile.totalMissionsCompleted + 1
        )
        val newAchievements = unlockAchievement(finalProfile)

        // Check level up AFTER achievements so all XP (mission + achievement bonus)
        // is already in the DB and a single pass handles every pending level.
        val profileForLevelCheck = userRepository.getUserProfile() ?: updatedProfile
        val levelUpResult = checkLevelUp(profileForLevelCheck)

        dataStore.setMissionRollbackEntry(
            mission.id,
            MissionPenaltyPolicy.buildCompletionRollback(mission, effectiveXp, hpRestored)
        )

        return CompletionResult(
            xpGained = effectiveXp,
            hpRestored = hpRestored,
            statGains = mission.statRewards.mapKeys { it.key.name },
            levelUpResult = if (levelUpResult.didLevelUp) levelUpResult else null,
            newAchievements = newAchievements,
            promotedToShadow = promotedToShadow
        )
    }
}
