package com.arise.habitquest.domain.usecase.policy

import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.ProgressionState

object ProgressionStatePolicy {

    fun nextState(
        current: ProgressionState,
        recentCompletionRate: Float,
        consecutiveMissDays: Int,
        safetyThrottle: Boolean
    ): ProgressionState {
        return when {
            consecutiveMissDays >= 2 || recentCompletionRate < 0.35f || safetyThrottle -> ProgressionState.DELOAD
            recentCompletionRate < 0.60f -> ProgressionState.HOLD
            current == ProgressionState.DELOAD || current == ProgressionState.HOLD -> ProgressionState.RE_RAMP
            else -> ProgressionState.PROGRESSING
        }
    }

    fun recommendTransition(
        goalCategories: Set<MissionCategory>,
        recentCompletionRate: Float,
        consecutiveMissDays: Int
    ): MissionCategory? {
        if (consecutiveMissDays < 3 && recentCompletionRate >= 0.45f) return null
        return when {
            MissionCategory.WELLNESS in goalCategories -> MissionCategory.WELLNESS
            MissionCategory.MENTAL in goalCategories -> MissionCategory.MENTAL
            else -> null
        }
    }
}
