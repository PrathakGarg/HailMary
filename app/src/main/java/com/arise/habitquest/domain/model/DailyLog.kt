package com.arise.habitquest.domain.model

import java.time.LocalDate

data class DailyLog(
    val date: LocalDate,
    val completedIds: List<String> = emptyList(),
    val failedIds: List<String> = emptyList(),
    val skippedIds: List<String> = emptyList(),
    val xpGained: Int = 0,
    val xpLost: Int = 0,
    val hpLost: Int = 0,
    val hpGained: Int = 0,
    val rankSnapshot: Rank = Rank.E,
    val levelSnapshot: Int = 1,
    val completionRate: Float = 0f,
    val totalMissions: Int = 0,
    val systemMessage: String = "",
    val wasRestDay: Boolean = false
) {
    val netXp: Int get() = xpGained - xpLost
    val netHp: Int get() = hpGained - hpLost
}
