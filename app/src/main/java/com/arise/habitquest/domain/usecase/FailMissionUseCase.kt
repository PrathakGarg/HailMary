package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionPenaltyPolicy
import javax.inject.Inject

data class FailResult(
    val xpDeducted: Int,
    val hpDeducted: Int,
    val isGraceDay: Boolean,       // true = no penalty applied yet
    val penaltyZoneTriggered: Boolean
)

class FailMissionUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val dataStore: OnboardingDataStore
) {
    suspend operator fun invoke(mission: Mission, profile: UserProfile): FailResult {
        missionRepository.markFailed(mission.id)

        val newMissDays = profile.consecutiveMissDays + 1
        val isGraceDay = newMissDays == 1 && !profile.pendingWarning

        val xpDeducted: Int
        val hpDeducted: Int

        if (isGraceDay) {
            // Grace: warn but apply no penalty
            xpDeducted = 0
            hpDeducted = 0
            userRepository.updateMissState(newMissDays, true)
        } else {
            val xpPenalty = MissionPenaltyPolicy.systemMandateXpPenalty(mission)
            val hpPenalty = MissionPenaltyPolicy.systemMandateHpPenalty(mission)

            xpDeducted = xpPenalty
            hpDeducted = hpPenalty

            val newXp = (profile.xp - xpDeducted).coerceAtLeast(0L)
            userRepository.updateXpAndLevel(newXp, profile.level, profile.rank, profile.xpToNextLevel)

            if (hpDeducted > 0) {
                val newHp = (profile.hp - hpDeducted).coerceAtLeast(0)
                userRepository.updateHp(newHp)
            }

            userRepository.updateMissState(newMissDays, false)
        }

        val penaltyZoneTriggered = !isGraceDay && (profile.hp - hpDeducted) <= 0

        dataStore.setMissionRollbackEntry(
            mission.id,
            MissionPenaltyPolicy.buildFailureRollback(xpDeducted, hpDeducted)
        )

        return FailResult(
            xpDeducted = xpDeducted,
            hpDeducted = hpDeducted,
            isGraceDay = isGraceDay,
            penaltyZoneTriggered = penaltyZoneTriggered
        )
    }
}
