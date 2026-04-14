package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.HunterStats
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionRollbackEntry
import com.arise.habitquest.domain.model.Stat
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import javax.inject.Inject

class ResetMissionOutcomeUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val dataStore: OnboardingDataStore
) {
    suspend operator fun invoke(missionId: String): Boolean {
        val mission = missionRepository.getMissionById(missionId) ?: return false
        if (mission.isActive) return false

        val rollbackEntry = sanitizeRollbackEntry(mission, dataStore.getMissionRollbackEntry(missionId))
        if (rollbackEntry != null) {
            val profile = userRepository.getUserProfile()
            if (profile != null) {
                val updatedXp = (profile.xp - rollbackEntry.xpDelta).coerceAtLeast(0L)
                val updatedHp = (profile.hp - rollbackEntry.hpDelta).coerceIn(0, profile.maxHp)

                val updatedStats = HunterStats(
                    str = (profile.stats.str - rollbackEntry.strDelta).coerceAtLeast(1),
                    agi = (profile.stats.agi - rollbackEntry.agiDelta).coerceAtLeast(1),
                    int = (profile.stats.int - rollbackEntry.intDelta).coerceAtLeast(1),
                    vit = (profile.stats.vit - rollbackEntry.vitDelta).coerceAtLeast(1),
                    end = (profile.stats.end - rollbackEntry.endDelta).coerceAtLeast(1),
                    sense = (profile.stats.sense - rollbackEntry.senseDelta).coerceAtLeast(1)
                )

                userRepository.updateXpAndLevel(updatedXp, profile.level, profile.rank, profile.xpToNextLevel)
                userRepository.updateHp(updatedHp)
                userRepository.updateStats(updatedStats)

                if (rollbackEntry.missionCountDelta > 0 || rollbackEntry.totalXpEarnedDelta > 0) {
                    userRepository.decrementMissionStats(
                        completedDelta = rollbackEntry.missionCountDelta,
                        xpDelta = rollbackEntry.totalXpEarnedDelta
                    )
                }
            }
        }

        missionRepository.resetOutcome(missionId)
        dataStore.removeMissionRollbackEntry(missionId)
        dataStore.pruneMissionRollbackLedger()
        return true
    }

    private fun sanitizeRollbackEntry(
        mission: Mission,
        entry: MissionRollbackEntry?
    ): MissionRollbackEntry? {
        val fallback = fallbackRollbackEntry(mission)
        if (entry == null) return fallback

        return when {
            mission.isCompleted -> {
                val expected = fallback
                MissionRollbackEntry(
                    recordedAtMillis = entry.recordedAtMillis,
                    xpDelta = sanitizePositiveRollback(entry.xpDelta, expected.xpDelta),
                    hpDelta = sanitizePositiveRollback(entry.hpDelta.toLong(), expected.hpDelta.toLong()).toInt(),
                    strDelta = sanitizePositiveRollback(entry.strDelta.toLong(), expected.strDelta.toLong()).toInt(),
                    agiDelta = sanitizePositiveRollback(entry.agiDelta.toLong(), expected.agiDelta.toLong()).toInt(),
                    intDelta = sanitizePositiveRollback(entry.intDelta.toLong(), expected.intDelta.toLong()).toInt(),
                    vitDelta = sanitizePositiveRollback(entry.vitDelta.toLong(), expected.vitDelta.toLong()).toInt(),
                    endDelta = sanitizePositiveRollback(entry.endDelta.toLong(), expected.endDelta.toLong()).toInt(),
                    senseDelta = sanitizePositiveRollback(entry.senseDelta.toLong(), expected.senseDelta.toLong()).toInt(),
                    missionCountDelta = sanitizePositiveRollback(entry.missionCountDelta.toLong(), expected.missionCountDelta.toLong()).toInt(),
                    totalXpEarnedDelta = sanitizePositiveRollback(entry.totalXpEarnedDelta, expected.totalXpEarnedDelta)
                )
            }

            mission.isFailed || mission.isSkipped -> {
                val expected = fallback
                MissionRollbackEntry(
                    recordedAtMillis = entry.recordedAtMillis,
                    xpDelta = entry.xpDelta.coerceIn(expected.xpDelta, 0L),
                    hpDelta = entry.hpDelta.coerceIn(expected.hpDelta, 0),
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

            else -> null
        }
    }

    private fun fallbackRollbackEntry(mission: Mission): MissionRollbackEntry {
        val effectiveXp = if (mission.acceptedMiniVersion) (mission.xpReward * 0.5f).toInt() else mission.xpReward
        return when {
            mission.isCompleted -> MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = effectiveXp.toLong(),
                hpDelta = 5,
                strDelta = mission.statRewards[Stat.STR] ?: 0,
                agiDelta = mission.statRewards[Stat.AGI] ?: 0,
                intDelta = mission.statRewards[Stat.INT] ?: 0,
                vitDelta = mission.statRewards[Stat.VIT] ?: 0,
                endDelta = mission.statRewards[Stat.END] ?: 0,
                senseDelta = mission.statRewards[Stat.SENSE] ?: 0,
                missionCountDelta = 1,
                totalXpEarnedDelta = effectiveXp.toLong()
            )

            mission.isFailed || mission.isSkipped -> MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = -if (mission.isSystemMandate) (mission.penaltyXp / 2).toLong() else mission.penaltyXp.toLong(),
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

    private fun sanitizePositiveRollback(actual: Long, expected: Long): Long {
        if (expected <= 0L) return 0L
        return if (actual in 1L..expected) actual else expected
    }
}
