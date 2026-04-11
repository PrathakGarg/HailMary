package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.lore.AchievementCatalogueExpanded
import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import com.arise.habitquest.domain.model.Rarity
import com.arise.habitquest.domain.repository.AchievementRepository
import javax.inject.Inject

class SeedAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke() {
        val achievements = buildCatalogue()
        achievementRepository.insertAchievements(achievements)
    }

    private fun buildCatalogue(): List<Achievement> = listOf(
        // ── Missions completed ─────────────────────────────────────────────
        Achievement(
            id = "ach_first_gate",
            title = "First Gate Cleared",
            description = "Complete your very first mission.",
            flavorText = "Every legend begins with a single step through a single gate.",
            iconName = "star",
            rarity = Rarity.COMMON,
            xpBonus = 50,
            triggerType = AchievementTrigger.MISSIONS_COMPLETED,
            triggerThreshold = 1
        ),
        Achievement(
            id = "ach_ten_gates",
            title = "Gate Runner",
            description = "Complete 10 missions total.",
            flavorText = "You are no longer a stranger to the dungeons.",
            iconName = "star_half",
            rarity = Rarity.COMMON,
            xpBonus = 100,
            triggerType = AchievementTrigger.MISSIONS_COMPLETED,
            triggerThreshold = 10
        ),
        Achievement(
            id = "ach_fifty_gates",
            title = "Veteran Hunter",
            description = "Complete 50 missions total.",
            flavorText = "Half a hundred gates lie behind you. More await.",
            iconName = "military_tech",
            rarity = Rarity.RARE,
            xpBonus = 300,
            triggerType = AchievementTrigger.MISSIONS_COMPLETED,
            triggerThreshold = 50
        ),
        Achievement(
            id = "ach_hundred_gates",
            title = "Century Slayer",
            description = "Complete 100 missions total.",
            flavorText = "The System catalogues your conquest. One hundred gates. Extraordinary.",
            iconName = "emoji_events",
            rarity = Rarity.EPIC,
            xpBonus = 750,
            triggerType = AchievementTrigger.MISSIONS_COMPLETED,
            triggerThreshold = 100
        ),
        Achievement(
            id = "ach_five_hundred",
            title = "Gate Sovereign",
            description = "Complete 500 missions total.",
            flavorText = "They feared the dungeons once. Now they fear you.",
            iconName = "workspace_premium",
            rarity = Rarity.LEGENDARY,
            xpBonus = 3000,
            triggerType = AchievementTrigger.MISSIONS_COMPLETED,
            triggerThreshold = 500
        ),

        // ── Streaks ────────────────────────────────────────────────────────
        Achievement(
            id = "ach_streak_3",
            title = "The Ritual Begins",
            description = "Maintain a 3-day streak.",
            flavorText = "Three days. The pattern is forming.",
            iconName = "local_fire_department",
            rarity = Rarity.COMMON,
            xpBonus = 75,
            triggerType = AchievementTrigger.STREAK_DAYS,
            triggerThreshold = 3
        ),
        Achievement(
            id = "ach_streak_7",
            title = "Week Warrior",
            description = "Maintain a 7-day streak.",
            flavorText = "One full revolution of the sun. A hunter's minimum.",
            iconName = "local_fire_department",
            rarity = Rarity.COMMON,
            xpBonus = 150,
            triggerType = AchievementTrigger.STREAK_DAYS,
            triggerThreshold = 7
        ),
        Achievement(
            id = "ach_streak_14",
            title = "Fortnight of Fire",
            description = "Maintain a 14-day streak.",
            flavorText = "Two weeks of consistency. Habit is forming.",
            iconName = "whatshot",
            rarity = Rarity.RARE,
            xpBonus = 300,
            triggerType = AchievementTrigger.STREAK_DAYS,
            triggerThreshold = 14
        ),
        Achievement(
            id = "ach_streak_21",
            title = "Shadow Summoner",
            description = "Maintain a 21-day streak.",
            flavorText = "Twenty-one days. The System recognises this as mastery. A shadow is born.",
            iconName = "auto_awesome",
            rarity = Rarity.EPIC,
            xpBonus = 500,
            triggerType = AchievementTrigger.STREAK_DAYS,
            triggerThreshold = 21
        ),
        Achievement(
            id = "ach_streak_30",
            title = "Monarch of Habit",
            description = "Maintain a 30-day streak.",
            flavorText = "Thirty days of pure dedication. Few reach this. You are one of them.",
            iconName = "diamond",
            rarity = Rarity.LEGENDARY,
            xpBonus = 1000,
            triggerType = AchievementTrigger.STREAK_DAYS,
            triggerThreshold = 30
        ),
        Achievement(
            id = "ach_streak_100",
            title = "The Undying Flame",
            description = "Maintain a 100-day streak.",
            flavorText = "One hundred days without pause. The System is silent. Even it is awed.",
            iconName = "flare",
            rarity = Rarity.MYTHIC,
            xpBonus = 5000,
            triggerType = AchievementTrigger.STREAK_DAYS,
            triggerThreshold = 100
        ),

        // ── Level reached ──────────────────────────────────────────────────
        Achievement(
            id = "ach_level_10",
            title = "The Awakening",
            description = "Reach Level 10.",
            flavorText = "You have shed your former weakness. The System takes notice.",
            iconName = "upgrade",
            rarity = Rarity.COMMON,
            xpBonus = 200,
            triggerType = AchievementTrigger.LEVEL_REACHED,
            triggerThreshold = 10
        ),
        Achievement(
            id = "ach_level_25",
            title = "Rising Hunter",
            description = "Reach Level 25.",
            flavorText = "A quarter century of power. The gatekeepers grow wary.",
            iconName = "trending_up",
            rarity = Rarity.RARE,
            xpBonus = 500,
            triggerType = AchievementTrigger.LEVEL_REACHED,
            triggerThreshold = 25
        ),
        Achievement(
            id = "ach_level_50",
            title = "Half a Legend",
            description = "Reach Level 50.",
            flavorText = "Halfway to the pinnacle. The System acknowledges your growth.",
            iconName = "star_border",
            rarity = Rarity.EPIC,
            xpBonus = 1500,
            triggerType = AchievementTrigger.LEVEL_REACHED,
            triggerThreshold = 50
        ),

        // ── Boss defeated ──────────────────────────────────────────────────
        Achievement(
            id = "ach_first_boss",
            title = "Raid Conqueror",
            description = "Complete your first Boss Raid.",
            flavorText = "The dungeon shook. You did not.",
            iconName = "shield",
            rarity = Rarity.RARE,
            xpBonus = 400,
            triggerType = AchievementTrigger.BOSS_DEFEATED,
            triggerThreshold = 1
        ),

        // ── Shadow unlocked ────────────────────────────────────────────────
        Achievement(
            id = "ach_first_shadow",
            title = "Shadow General",
            description = "Convert a habit into a Shadow (21-day streak).",
            flavorText = "The shadow rises. It needs no command. It simply follows.",
            iconName = "dark_mode",
            rarity = Rarity.EPIC,
            xpBonus = 500,
            triggerType = AchievementTrigger.SHADOW_UNLOCKED,
            triggerThreshold = 1
        )
    ) + AchievementCatalogueExpanded.allExpanded
}
