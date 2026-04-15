package com.arise.habitquest.domain.model

import java.time.Instant

enum class Rarity(val displayName: String) {
    COMMON("Common"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary"),
    MYTHIC("Mythic");

    companion object {
        fun fromString(s: String): Rarity = entries.find { it.name == s } ?: COMMON
    }
}

enum class AchievementTrigger {
    MISSIONS_COMPLETED,
    STREAK_DAYS,
    LEVEL_REACHED,
    RANK_REACHED,
    HP_ZERO,
    SHADOW_UNLOCKED,
    EARLY_BIRD,          // Complete all missions before noon
    NIGHT_OWL,           // Complete missions after 10pm
    PERFECT_WEEK,        // 100% completion for 7 days
    BOSS_DEFEATED,
    XP_TOTAL,            // Total lifetime XP earned
    MANUAL;

    companion object {
        fun fromString(s: String): AchievementTrigger = entries.find { it.name == s } ?: MANUAL
    }
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val flavorText: String,
    val iconName: String,
    val rarity: Rarity,
    val unlockedAt: Instant? = null,
    val progressCurrent: Int = 0,
    val progressTarget: Int = 1,
    val xpBonus: Int = 0,
    val triggerType: AchievementTrigger = AchievementTrigger.MANUAL,
    val triggerThreshold: Int = 1
) {
    val isUnlocked: Boolean get() = unlockedAt != null
    val progressPercent: Float get() = progressPercent(progressCurrent, progressTarget)
}
