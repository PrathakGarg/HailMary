package com.arise.habitquest.domain.usecase

import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.AchievementRepository
import com.arise.habitquest.domain.repository.UserRepository
import javax.inject.Inject

class UnlockAchievementUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(profile: UserProfile): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        // Check missions-completed achievements
        checkTrigger(
            trigger = AchievementTrigger.MISSIONS_COMPLETED,
            value = profile.totalMissionsCompleted,
            newlyUnlocked = newlyUnlocked
        )

        // Check streak achievements
        checkTrigger(
            trigger = AchievementTrigger.STREAK_DAYS,
            value = profile.streakCurrent,
            newlyUnlocked = newlyUnlocked
        )

        // Check level achievements
        checkTrigger(
            trigger = AchievementTrigger.LEVEL_REACHED,
            value = profile.level,
            newlyUnlocked = newlyUnlocked
        )

        // Award XP bonuses from newly unlocked achievements
        val totalBonusXp = newlyUnlocked.sumOf { it.xpBonus }
        if (totalBonusXp > 0) {
            val updatedProfile = profile.copy(xp = profile.xp + totalBonusXp)
            userRepository.updateXpAndLevel(
                updatedProfile.xp,
                updatedProfile.level,
                updatedProfile.rank,
                updatedProfile.xpToNextLevel
            )
        }

        return newlyUnlocked
    }

    private suspend fun checkTrigger(
        trigger: AchievementTrigger,
        value: Int,
        newlyUnlocked: MutableList<Achievement>
    ) {
        val candidates = achievementRepository.getAchievementsByTrigger(trigger)
        for (achievement in candidates) {
            if (achievement.isUnlocked) continue
            // Update progress
            achievementRepository.updateProgress(achievement.id, value)
            // Check if threshold met
            if (value >= achievement.triggerThreshold) {
                achievementRepository.unlockAchievement(achievement.id)
                newlyUnlocked.add(achievement)
            }
        }
    }
}
