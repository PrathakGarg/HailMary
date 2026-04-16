package com.arise.habitquest.domain.model

import java.time.LocalDate

enum class MissionType(val displayName: String) {
    DAILY("Daily Gate"),
    WEEKLY("Weekly Quest"),
    BOSS_RAID("Boss Raid"),
    SPECIAL("Special Mission"),
    PENALTY_ZONE("Penalty Zone")
}

enum class MissionCategory(val displayName: String, val iconName: String) {
    PHYSICAL("Physical", "fitness_center"),
    MENTAL("Mental", "psychology"),
    PRODUCTIVITY("Productivity", "task_alt"),
    SOCIAL("Social", "group"),
    WELLNESS("Wellness", "spa"),
    CREATIVITY("Creativity", "palette")
}

enum class Difficulty(val displayName: String, val baseXpMultiplier: Float) {
    F("F", 0.3f),
    E("E", 0.5f),
    D("D", 0.75f),
    C("C", 1.0f),
    B("B", 1.5f),
    A("A", 2.0f),
    S("S", 3.0f);

    companion object {
        fun fromString(s: String): Difficulty = entries.find { it.name == s } ?: E
    }
}

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val systemLore: String,
    val miniMissionDescription: String = "",
    val type: MissionType,
    val category: MissionCategory,
    val difficulty: Difficulty,
    val xpReward: Int,
    val penaltyXp: Int,
    val penaltyHp: Int,
    val statRewards: Map<Stat, Int> = emptyMap(),
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val isSkipped: Boolean = false,
    val acceptedMiniVersion: Boolean = false,
    val dueDate: LocalDate,
    val scheduledTimeHint: String? = null,
    val streakCount: Int = 0,
    val isRecurring: Boolean = true,
    val parentTemplateId: String? = null,
    val progressCurrent: Int = 0,
    val progressTarget: Int = 1,
    val iconName: String = "fitness_center",
    val isSystemMandate: Boolean = false,   // Injected by System for all-round growth
    val physicalFamily: PhysicalMissionFamily = PhysicalMissionFamily.UNSPECIFIED,
    val muscleLoad: Map<MuscleRegion, Float> = emptyMap()
) {
    val isActive: Boolean get() = !isCompleted && !isFailed && !isSkipped
    val progressPercent: Float get() = progressPercent(progressCurrent, progressTarget)
    val effectiveXpReward: Int get() = if (acceptedMiniVersion) (xpReward * 0.5f).toInt() else xpReward
}
