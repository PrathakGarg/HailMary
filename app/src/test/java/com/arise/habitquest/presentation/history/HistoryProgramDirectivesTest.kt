package com.arise.habitquest.presentation.history

import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MuscleRegion
import com.arise.habitquest.domain.model.ProgressionState
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.presentation.progression.buildProgramDirectives
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryProgramDirectivesTest {

    @Test
    fun buildProgramDirectives_includesProgressionTransitionAndCoverageRationale() {
        val directives = buildProgramDirectives(
            profile = UserProfile(
                hunterName = "tester",
                progressionState = ProgressionState.DELOAD,
                transitionRecommendation = MissionCategory.WELLNESS
            ),
            weeklyCoverage = listOf(
                MuscleCoverageEntry(
                    region = MuscleRegion.CORE,
                    assignedLoad = 1f,
                    completedLoad = 0.2f,
                    completionRatio = 0.2f
                )
            ),
            recentCompletionRate = 0.32f
        )

        assertEquals(3, directives.size)
        assertEquals("PROGRESSION STATE", directives[0].label)
        assertEquals("Deload", directives[0].value)
        assertTrue(directives[0].detail.contains("32%"))
        assertEquals("TRANSITION WATCH", directives[1].label)
        assertTrue(directives[1].value.contains("Wellness"))
        assertEquals("COVERAGE CORRECTION", directives[2].label)
        assertTrue(directives[2].value.contains("Core"))
    }

    @Test
    fun buildProgramDirectives_keepsOnlyStableProgressionDirectiveWhenSignalsAreClean() {
        val directives = buildProgramDirectives(
            profile = UserProfile(
                hunterName = "tester",
                progressionState = ProgressionState.PROGRESSING,
                transitionRecommendation = null
            ),
            weeklyCoverage = listOf(
                MuscleCoverageEntry(
                    region = MuscleRegion.QUADS,
                    assignedLoad = 1f,
                    completedLoad = 0.8f,
                    completionRatio = 0.8f
                )
            ),
            recentCompletionRate = 0.84f
        )

        assertEquals(1, directives.size)
        assertEquals("PROGRESSION STATE", directives.single().label)
        assertEquals("Progressing", directives.single().value)
        assertTrue(directives.single().detail.contains("84%"))
    }
}