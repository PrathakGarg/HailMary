package com.arise.habitquest.domain.model

import java.time.LocalDate

data class Shadow(
    val templateId: String,
    val name: String,
    val category: MissionCategory,
    val description: String,
    val totalCompletions: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isShadow: Boolean = false,
    val lastCompletedDate: LocalDate? = null,
    val iconName: String = "auto_awesome",
    val xpPerCompletion: Int = 30
) {
    val progressToShadow: Float get() = if (!isShadow) (currentStreak / 21f).coerceAtMost(1f) else 1f
    val daysToShadow: Int get() = if (!isShadow) (21 - currentStreak).coerceAtLeast(0) else 0
}
