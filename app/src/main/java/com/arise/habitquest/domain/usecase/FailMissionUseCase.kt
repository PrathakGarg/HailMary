package com.arise.habitquest.domain.usecase

import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import javax.inject.Inject

data class FailResult(
    val xpDeducted: Int,
    val hpDeducted: Int,
    val isGraceDay: Boolean,       // true = no penalty applied yet
    val penaltyZoneTriggered: Boolean
)

class FailMissionUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository
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
            // Day 2+ consecutive miss: apply penalties.
            // System Mandates (cross-category growth missions) carry softer consequences —
            // the System won't brutally punish hunters for struggling outside their expertise,
            // but there are still consequences for ignoring the directive.
            val xpPenalty = if (mission.isSystemMandate) mission.penaltyXp / 2 else mission.penaltyXp
            val hpPenalty = if (mission.isSystemMandate) 0 else mission.penaltyHp

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

        return FailResult(
            xpDeducted = xpDeducted,
            hpDeducted = hpDeducted,
            isGraceDay = isGraceDay,
            penaltyZoneTriggered = penaltyZoneTriggered
        )
    }
}
