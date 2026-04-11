package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.UserRepository
import javax.inject.Inject

data class LevelUpResult(
    val didLevelUp: Boolean,
    val didRankUp: Boolean,
    val newLevel: Int,
    val newRank: Rank,
    val newXpToNext: Long
)

class CheckLevelUpUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val generator: MissionGenerator,
    private val dataStore: OnboardingDataStore
) {
    suspend operator fun invoke(profile: UserProfile): LevelUpResult {
        var currentXp = profile.xp
        var currentLevel = profile.level
        var currentRank = profile.rank
        var didLevelUp = false
        var didRankUp = false

        var xpNeeded = generator.xpForLevel(currentLevel, currentRank)

        while (currentXp >= xpNeeded) {
            currentXp -= xpNeeded
            currentLevel++
            didLevelUp = true

            if (currentLevel > 100) {
                val nextRank = currentRank.next()
                if (nextRank != currentRank) {
                    currentRank = nextRank
                    didRankUp = true
                }
                currentLevel = 1
            }

            xpNeeded = generator.xpForLevel(currentLevel, currentRank)
        }

        if (didLevelUp) {
            userRepository.updateXpAndLevel(currentXp, currentLevel, currentRank, xpNeeded)
        }

        // Store pending rank-up so Home screen can navigate to RankUp screen
        if (didRankUp) {
            dataStore.setPendingRankUp(currentRank.name)
        }

        return LevelUpResult(didLevelUp, didRankUp, currentLevel, currentRank, xpNeeded)
    }
}
