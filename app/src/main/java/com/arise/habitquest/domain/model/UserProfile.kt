package com.arise.habitquest.domain.model

import java.time.LocalDate

enum class Rank(val displayName: String, val order: Int) {
    E("E", 0), D("D", 1), C("C", 2), B("B", 3),
    A("A", 4), S("S", 5), SS("SS", 6), SSS("SSS", 7), MONARCH("MONARCH", 8);

    fun next(): Rank = entries.getOrElse(order + 1) { MONARCH }

    companion object {
        fun fromString(s: String): Rank = entries.find { it.name == s } ?: E
    }
}

enum class Stat { STR, AGI, INT, VIT, END, SENSE }

data class HunterStats(
    val str: Int = 5,
    val agi: Int = 5,
    val int: Int = 5,
    val vit: Int = 5,
    val end: Int = 5,
    val sense: Int = 5
) {
    fun get(stat: Stat): Int = when (stat) {
        Stat.STR -> str
        Stat.AGI -> agi
        Stat.INT -> int
        Stat.VIT -> vit
        Stat.END -> end
        Stat.SENSE -> sense
    }

    fun add(statDeltas: Map<Stat, Int>): HunterStats = copy(
        str = (str + (statDeltas[Stat.STR] ?: 0)).coerceAtMost(999),
        agi = (agi + (statDeltas[Stat.AGI] ?: 0)).coerceAtMost(999),
        int = (int + (statDeltas[Stat.INT] ?: 0)).coerceAtMost(999),
        vit = (vit + (statDeltas[Stat.VIT] ?: 0)).coerceAtMost(999),
        end = (end + (statDeltas[Stat.END] ?: 0)).coerceAtMost(999),
        sense = (sense + (statDeltas[Stat.SENSE] ?: 0)).coerceAtMost(999)
    )
}

data class UserProfile(
    val id: Int = 1,
    val hunterName: String = "",
    val epithet: String = "",
    val title: String = "The Unawakened",
    val rank: Rank = Rank.E,
    val level: Int = 1,
    val xp: Long = 0L,
    val xpToNextLevel: Long = 100L,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val stats: HunterStats = HunterStats(),
    val streakCurrent: Int = 0,
    val streakBest: Int = 0,
    val daysSinceJoin: Int = 0,
    val totalMissionsCompleted: Int = 0,
    val totalXpEarned: Long = 0L,
    val streakShields: Int = 0,
    val graceUsesRemaining: Int = 3,
    val adaptiveDifficulty: Float = 1.0f,
    val restDay: Int = 0,
    val notificationHour: Int = 8,
    val onboardingComplete: Boolean = false,
    val onboardingAnswersJson: String = "",
    val joinDate: LocalDate? = null,
    val pendingWarning: Boolean = false,
    val consecutiveMissDays: Int = 0
) {
    val hpPercent: Float get() = if (maxHp > 0) hp.toFloat() / maxHp else 0f
    val xpPercent: Float get() = if (xpToNextLevel > 0) xp.toFloat() / xpToNextLevel else 0f
    val isAtRisk: Boolean get() = hp <= maxHp * 0.25f
    val isInPenaltyZone: Boolean get() = hp <= 0
}
