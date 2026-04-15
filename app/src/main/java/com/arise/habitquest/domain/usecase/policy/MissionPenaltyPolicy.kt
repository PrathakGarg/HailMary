package com.arise.habitquest.domain.usecase.policy

import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionRollbackEntry
import com.arise.habitquest.domain.model.Stat

object MissionPenaltyPolicy {

    fun systemMandateXpPenalty(mission: Mission): Int =
        if (mission.isSystemMandate) mission.penaltyXp / 2 else mission.penaltyXp

    fun systemMandateHpPenalty(mission: Mission): Int =
        if (mission.isSystemMandate) 0 else mission.penaltyHp

    fun buildCompletionRollback(mission: Mission, effectiveXp: Int, hpRestored: Int): MissionRollbackEntry =
        MissionRollbackEntry(
            recordedAtMillis = System.currentTimeMillis(),
            xpDelta = effectiveXp.toLong(),
            hpDelta = hpRestored,
            strDelta = mission.statRewards[Stat.STR] ?: 0,
            agiDelta = mission.statRewards[Stat.AGI] ?: 0,
            intDelta = mission.statRewards[Stat.INT] ?: 0,
            vitDelta = mission.statRewards[Stat.VIT] ?: 0,
            endDelta = mission.statRewards[Stat.END] ?: 0,
            senseDelta = mission.statRewards[Stat.SENSE] ?: 0,
            missionCountDelta = 1,
            totalXpEarnedDelta = effectiveXp.toLong()
        )

    fun buildFailureRollback(xpDeducted: Int, hpDeducted: Int): MissionRollbackEntry =
        MissionRollbackEntry(
            recordedAtMillis = System.currentTimeMillis(),
            xpDelta = -xpDeducted.toLong(),
            hpDelta = -hpDeducted,
            strDelta = 0,
            agiDelta = 0,
            intDelta = 0,
            vitDelta = 0,
            endDelta = 0,
            senseDelta = 0,
            missionCountDelta = 0,
            totalXpEarnedDelta = 0L
        )

    fun buildFallbackRollback(mission: Mission): MissionRollbackEntry {
        val effectiveXp = if (mission.acceptedMiniVersion) (mission.xpReward * 0.5f).toInt() else mission.xpReward
        return when {
            mission.isCompleted -> buildCompletionRollback(mission, effectiveXp, 5)
            mission.isFailed || mission.isSkipped -> MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = -systemMandateXpPenalty(mission).toLong(),
                hpDelta = if (mission.isSystemMandate) 0 else -mission.penaltyHp,
                strDelta = 0,
                agiDelta = 0,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = 0,
                totalXpEarnedDelta = 0L
            )
            else -> MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = 0,
                hpDelta = 0,
                strDelta = 0,
                agiDelta = 0,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = 0,
                totalXpEarnedDelta = 0L
            )
        }
    }
}
