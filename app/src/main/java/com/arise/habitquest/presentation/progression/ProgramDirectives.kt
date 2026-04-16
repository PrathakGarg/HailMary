package com.arise.habitquest.presentation.progression

import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.ProgressionState
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.presentation.history.MuscleCoverageEntry

data class ProgramDirective(
    val label: String,
    val value: String,
    val detail: String,
    val isWarning: Boolean
)

fun buildProgramDirectives(
    profile: UserProfile?,
    weeklyCoverage: List<MuscleCoverageEntry>,
    recentCompletionRate: Float
): List<ProgramDirective> {
    if (profile == null) return emptyList()

    val directives = mutableListOf<ProgramDirective>()
    val completionPercent = (recentCompletionRate * 100).toInt()

    val progressionDetail = when (profile.progressionState) {
        ProgressionState.DELOAD ->
            "Recent completion is $completionPercent%. The system is temporarily reducing pressure to stabilize consistency."
        ProgressionState.HOLD ->
            "Recent completion is $completionPercent%. The current phase is being held until execution steadies."
        ProgressionState.RE_RAMP ->
            "Recent completion is $completionPercent%. Recovery signals are strong enough to start scaling back up."
        ProgressionState.PROGRESSING ->
            "Recent completion is $completionPercent%. Compliance is stable enough to continue steady progression."
    }

    directives += ProgramDirective(
        label = "PROGRESSION STATE",
        value = profile.progressionState.displayName(),
        detail = progressionDetail,
        isWarning = profile.progressionState == ProgressionState.DELOAD || profile.progressionState == ProgressionState.HOLD
    )

    profile.transitionRecommendation?.let { recommendation ->
        directives += ProgramDirective(
            label = "TRANSITION WATCH",
            value = "Bias toward ${recommendation.displayName}",
            detail = "Recent struggle signals suggest shifting emphasis toward ${recommendation.displayName.lowercase()} until momentum returns.",
            isWarning = false
        )
    }

    val underCoveredRegions = weeklyCoverage
        .filter { it.assignedLoad > 0f && it.completionRatio < 0.45f }
        .sortedBy { it.completionRatio }
        .take(2)

    if (underCoveredRegions.isNotEmpty()) {
        val regionNames = underCoveredRegions.joinToString(" / ") {
            it.region.name.lowercase().replace('_', ' ').replaceFirstChar { c -> c.uppercase() }
        }
        directives += ProgramDirective(
            label = "COVERAGE CORRECTION",
            value = regionNames,
            detail = "These areas are lagging below 45% completion this week, so upcoming assignments will bias them until the gap narrows.",
            isWarning = true
        )
    }

    return directives
}

fun ProgressionState.displayName(): String = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

fun MissionCategory.displayDirectiveLabel(): String = displayName