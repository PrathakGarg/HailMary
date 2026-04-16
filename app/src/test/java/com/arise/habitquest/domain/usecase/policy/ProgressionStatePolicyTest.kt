package com.arise.habitquest.domain.usecase.policy

import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.ProgressionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressionStatePolicyTest {

    @Test
    fun nextState_entersDeloadOnHighRiskSignals() {
        val state = ProgressionStatePolicy.nextState(
            current = ProgressionState.PROGRESSING,
            recentCompletionRate = 0.72f,
            consecutiveMissDays = 2,
            safetyThrottle = false
        )

        assertEquals(ProgressionState.DELOAD, state)
    }

    @Test
    fun nextState_entersHoldOnModerateUnderperformance() {
        val state = ProgressionStatePolicy.nextState(
            current = ProgressionState.PROGRESSING,
            recentCompletionRate = 0.55f,
            consecutiveMissDays = 0,
            safetyThrottle = false
        )

        assertEquals(ProgressionState.HOLD, state)
    }

    @Test
    fun nextState_reRampsAfterRecoveryFromHoldOrDeload() {
        val state = ProgressionStatePolicy.nextState(
            current = ProgressionState.DELOAD,
            recentCompletionRate = 0.81f,
            consecutiveMissDays = 0,
            safetyThrottle = false
        )

        assertEquals(ProgressionState.RE_RAMP, state)
    }

    @Test
    fun recommendTransition_prefersWellnessForSustainedStruggle() {
        val recommendation = ProgressionStatePolicy.recommendTransition(
            goalCategories = setOf(MissionCategory.PHYSICAL, MissionCategory.WELLNESS),
            recentCompletionRate = 0.30f,
            consecutiveMissDays = 3
        )

        assertEquals(MissionCategory.WELLNESS, recommendation)
    }

    @Test
    fun recommendTransition_noneWhenSignalsAreStable() {
        val recommendation = ProgressionStatePolicy.recommendTransition(
            goalCategories = setOf(MissionCategory.PHYSICAL, MissionCategory.WELLNESS),
            recentCompletionRate = 0.76f,
            consecutiveMissDays = 0
        )

        assertNull(recommendation)
    }
}
