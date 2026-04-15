package com.arise.habitquest.domain.usecase

import android.util.Log
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.HunterStats
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionRollbackEntry
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionPenaltyPolicy
import javax.inject.Inject

class ResetMissionOutcomeUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val dataStore: OnboardingDataStore
) {
    suspend operator fun invoke(missionId: String): Boolean {
        return runCatching {
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
            true
        }.getOrElse {
            Log.e("ResetMissionOutcome", "Reset failed for missionId=$missionId", it)
            // Safety net: never crash UI on reset; best-effort state reset only.
            runCatching { missionRepository.resetOutcome(missionId) }
            runCatching { dataStore.removeMissionRollbackEntry(missionId) }
            runCatching { dataStore.pruneMissionRollbackLedger() }
            false
        }
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

    private fun fallbackRollbackEntry(mission: Mission): MissionRollbackEntry =
        MissionPenaltyPolicy.buildFallbackRollback(mission)

    private fun sanitizePositiveRollback(actual: Long, expected: Long): Long {
        if (expected <= 0L) return 0L
        return if (actual in 1L..expected) actual else expected
    }
}
