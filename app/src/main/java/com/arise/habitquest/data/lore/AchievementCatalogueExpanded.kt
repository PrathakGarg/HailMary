package com.arise.habitquest.data.lore

import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import com.arise.habitquest.domain.model.Rarity

/**
 * Expanded achievement catalogue — 35 new achievements.
 * Merge [allExpanded] into the existing SeedAchievementsUseCase catalogue to activate.
 *
 * Covers: category mastery, special behaviours, recovery, meta milestones, and hidden/fun achievements.
 * Does NOT duplicate: ach_first_gate, ach_ten_gates, ach_fifty_gates, ach_hundred_gates, ach_five_hundred,
 *   ach_streak_3/7/14/21/30/100, ach_level_10/25/50, ach_first_boss, ach_first_shadow.
 */
object AchievementCatalogueExpanded {

    // ── PHYSICAL category mastery (10 / 25 / 50) ──────────────────────────────

    val physicalTen = Achievement(
        id = "ach_physical_10",
        title = "Iron in the Blood",
        description = "Complete 10 Physical missions.",
        flavorText = "The System detects elevated density in your muscle fibres. Continue.",
        iconName = "fitness_center",
        rarity = Rarity.COMMON,
        xpBonus = 120,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 10
    )

    val physicalTwentyFive = Achievement(
        id = "ach_physical_25",
        title = "The Iron Body",
        description = "Complete 25 Physical missions.",
        flavorText = "Your body is no longer a passenger. The System classifies it as a weapon.",
        iconName = "fitness_center",
        rarity = Rarity.RARE,
        xpBonus = 300,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 25
    )

    val physicalFifty = Achievement(
        id = "ach_physical_50",
        title = "Flesh Forged in Gates",
        description = "Complete 50 Physical missions.",
        flavorText = "Fifty physical gates cleared. The System notes: your vessel has become something else entirely.",
        iconName = "fitness_center",
        rarity = Rarity.EPIC,
        xpBonus = 750,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 50
    )

    // ── MENTAL category mastery (10 / 25 / 50) ────────────────────────────────

    val mentalTen = Achievement(
        id = "ach_mental_10",
        title = "The Mind Stirs",
        description = "Complete 10 Mental missions.",
        flavorText = "Cognitive activity detected above baseline. The System is taking notes.",
        iconName = "psychology",
        rarity = Rarity.COMMON,
        xpBonus = 120,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 10
    )

    val mentalTwentyFive = Achievement(
        id = "ach_mental_25",
        title = "The Sharpened Edge",
        description = "Complete 25 Mental missions.",
        flavorText = "The System registers your intelligence coefficient as notably elevated. You are harder to deceive.",
        iconName = "psychology",
        rarity = Rarity.RARE,
        xpBonus = 300,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 25
    )

    val mentalFifty = Achievement(
        id = "ach_mental_50",
        title = "The Living Library",
        description = "Complete 50 Mental missions.",
        flavorText = "Fifty gates of the mind cleared. The System has upgraded your intelligence classification to 'Formidable.'",
        iconName = "psychology",
        rarity = Rarity.EPIC,
        xpBonus = 750,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 50
    )

    // ── WELLNESS category mastery (10 / 25 / 50) ──────────────────────────────

    val wellnessTen = Achievement(
        id = "ach_wellness_10",
        title = "The Tended Vessel",
        description = "Complete 10 Wellness missions.",
        flavorText = "The System confirms: your body is being maintained, not merely used.",
        iconName = "spa",
        rarity = Rarity.COMMON,
        xpBonus = 120,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 10
    )

    val wellnessTwentyFive = Achievement(
        id = "ach_wellness_25",
        title = "The Restored Hunter",
        description = "Complete 25 Wellness missions.",
        flavorText = "Twenty-five acts of genuine self-care. The System records this as an unusual commitment to longevity.",
        iconName = "spa",
        rarity = Rarity.RARE,
        xpBonus = 300,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 25
    )

    val wellnessFifty = Achievement(
        id = "ach_wellness_50",
        title = "The Unbreakable Vessel",
        description = "Complete 50 Wellness missions.",
        flavorText = "Fifty wellness gates. The System has never seen a hunter maintain their body like this. You will outlast them all.",
        iconName = "spa",
        rarity = Rarity.EPIC,
        xpBonus = 750,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 50
    )

    // ── PRODUCTIVITY category mastery (10 / 25 / 50) ─────────────────────────

    val productivityTen = Achievement(
        id = "ach_productivity_10",
        title = "The Tactical Hunter",
        description = "Complete 10 Productivity missions.",
        flavorText = "Your operational efficiency is no longer accidental. The System approves.",
        iconName = "task_alt",
        rarity = Rarity.COMMON,
        xpBonus = 120,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 10
    )

    val productivityTwentyFive = Achievement(
        id = "ach_productivity_25",
        title = "Entropy's Nemesis",
        description = "Complete 25 Productivity missions.",
        flavorText = "The System calculates that you have eliminated approximately 60 hours of wasted time. You are dangerous to chaos.",
        iconName = "task_alt",
        rarity = Rarity.RARE,
        xpBonus = 300,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 25
    )

    val productivityFifty = Achievement(
        id = "ach_productivity_50",
        title = "The Clockwork Sovereign",
        description = "Complete 50 Productivity missions.",
        flavorText = "Fifty gates of order. The System notes you have bent time to your will more effectively than most hunters bend steel.",
        iconName = "task_alt",
        rarity = Rarity.EPIC,
        xpBonus = 750,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 50
    )

    // ── SOCIAL category mastery (10 / 25 / 50) ────────────────────────────────

    val socialTen = Achievement(
        id = "ach_social_10",
        title = "The Guild Seed",
        description = "Complete 10 Social missions.",
        flavorText = "Ten genuine connections made. The System detects the early formation of a support structure around you.",
        iconName = "group",
        rarity = Rarity.COMMON,
        xpBonus = 120,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 10
    )

    val socialTwentyFive = Achievement(
        id = "ach_social_25",
        title = "The Thread Weaver",
        description = "Complete 25 Social missions.",
        flavorText = "The System has mapped your relational network. It is growing in ways that compound. The Isolation Specter fears you.",
        iconName = "group",
        rarity = Rarity.RARE,
        xpBonus = 300,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 25
    )

    val socialFifty = Achievement(
        id = "ach_social_50",
        title = "Architect of Bonds",
        description = "Complete 50 Social missions.",
        flavorText = "Fifty social gates. You do not fight dungeons alone. You never did. Now you have built the proof.",
        iconName = "group",
        rarity = Rarity.EPIC,
        xpBonus = 750,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 50
    )

    // ── CREATIVITY category mastery (10 / 25 / 50) ────────────────────────────

    val creativityTen = Achievement(
        id = "ach_creativity_10",
        title = "The Spark",
        description = "Complete 10 Creativity missions.",
        flavorText = "Ten acts of creation. The System detects an anomaly: you are building something that did not exist before.",
        iconName = "palette",
        rarity = Rarity.COMMON,
        xpBonus = 120,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 10
    )

    val creativityTwentyFive = Achievement(
        id = "ach_creativity_25",
        title = "The Forge Keeper",
        description = "Complete 25 Creativity missions.",
        flavorText = "Twenty-five creations in the log. The System cannot predict what you will make next. That is exactly as it should be.",
        iconName = "palette",
        rarity = Rarity.RARE,
        xpBonus = 300,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 25
    )

    val creativityFifty = Achievement(
        id = "ach_creativity_50",
        title = "The Maker's Mark",
        description = "Complete 50 Creativity missions.",
        flavorText = "Fifty gates of creation. The System marks your existence in the record with a single note: 'This one builds.'",
        iconName = "palette",
        rarity = Rarity.EPIC,
        xpBonus = 750,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 50
    )

    // ── Special behaviours ────────────────────────────────────────────────────

    val earlyBirdMission = Achievement(
        id = "ach_before_7am",
        title = "Before the World Wakes",
        description = "Complete a morning mission before 7:00 AM.",
        flavorText = "The System registered your activity at a time when most hunters are still unconscious. Noted.",
        iconName = "wb_twilight",
        rarity = Rarity.RARE,
        xpBonus = 200,
        triggerType = AchievementTrigger.EARLY_BIRD,
        triggerThreshold = 1
    )

    val sundaySweep = Achievement(
        id = "ach_sunday_complete",
        title = "No Day Off",
        description = "Complete all assigned missions on a Sunday.",
        flavorText = "The day everyone else rests, you worked. The System finds this significant.",
        iconName = "calendar_today",
        rarity = Rarity.RARE,
        xpBonus = 150,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 1
    )

    val firstBossRaid = Achievement(
        id = "ach_boss_raid_complete",
        title = "Raid Veteran",
        description = "Complete your first Boss Raid week.",
        flavorText = "The dungeon shuddered. You did not flinch. The System has updated your threat classification.",
        iconName = "shield",
        rarity = Rarity.RARE,
        xpBonus = 400,
        triggerType = AchievementTrigger.BOSS_DEFEATED,
        triggerThreshold = 1
    )

    val threeShadows = Achievement(
        id = "ach_three_shadows",
        title = "The Shadow Army Grows",
        description = "Have 3 active Shadows simultaneously.",
        flavorText = "Three habits so deeply ingrained they no longer require your will. The System observes your army with something resembling awe.",
        iconName = "dark_mode",
        rarity = Rarity.EPIC,
        xpBonus = 600,
        triggerType = AchievementTrigger.SHADOW_UNLOCKED,
        triggerThreshold = 3
    )

    val midnightMission = Achievement(
        id = "ach_midnight_mission",
        title = "The Night Shift",
        description = "Complete a mission between midnight and 1:00 AM.",
        flavorText = "The System did not expect activity at this hour. It respects the dedication, even if it questions the sleep schedule.",
        iconName = "nights_stay",
        rarity = Rarity.RARE,
        xpBonus = 175,
        triggerType = AchievementTrigger.NIGHT_OWL,
        triggerThreshold = 1
    )

    // ── Recovery achievements ─────────────────────────────────────────────────

    val comeback = Achievement(
        id = "ach_comeback",
        title = "The Return",
        description = "Complete a mission the day after a zero-completion day.",
        flavorText = "You fell. You got up. The System marks this as more important than you realise.",
        iconName = "replay",
        rarity = Rarity.COMMON,
        xpBonus = 100,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 1
    )

    val perfectRecovery = Achievement(
        id = "ach_perfect_after_partial",
        title = "Full Reckoning",
        description = "Achieve 100% mission completion the day after a partial day.",
        flavorText = "Yesterday was incomplete. Today was not. The System records this as a character statement.",
        iconName = "check_circle",
        rarity = Rarity.RARE,
        xpBonus = 200,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 1
    )

    // ── Meta milestones: total XP ─────────────────────────────────────────────

    val xpFiveHundred = Achievement(
        id = "ach_xp_500",
        title = "Power Accumulates",
        description = "Earn a total of 500 XP.",
        flavorText = "The System has logged 500 units of effort. This is where most give up. You are not most.",
        iconName = "bolt",
        rarity = Rarity.COMMON,
        xpBonus = 50,
        triggerType = AchievementTrigger.XP_TOTAL,
        triggerThreshold = 500
    )

    val xpTwoThousand = Achievement(
        id = "ach_xp_2000",
        title = "The Accumulator",
        description = "Earn a total of 2,000 XP.",
        flavorText = "Two thousand units of discipline converted to power. The System notes the compounding effect is beginning.",
        iconName = "bolt",
        rarity = Rarity.RARE,
        xpBonus = 200,
        triggerType = AchievementTrigger.XP_TOTAL,
        triggerThreshold = 2000
    )

    val xpFiveThousand = Achievement(
        id = "ach_xp_5000",
        title = "The Reservoir",
        description = "Earn a total of 5,000 XP.",
        flavorText = "Five thousand. The System has run the statistics. You are in the top percentile of all hunters who have ever opened a gate.",
        iconName = "bolt",
        rarity = Rarity.EPIC,
        xpBonus = 500,
        triggerType = AchievementTrigger.XP_TOTAL,
        triggerThreshold = 5000
    )

    // ── Meta milestones: rank reached ─────────────────────────────────────────

    val rankD = Achievement(
        id = "ach_rank_d",
        title = "No Longer the Bottom",
        description = "Reach Rank D.",
        flavorText = "You have crossed the first real threshold. The System has updated your classification. You are now visible to it.",
        iconName = "military_tech",
        rarity = Rarity.COMMON,
        xpBonus = 150,
        triggerType = AchievementTrigger.RANK_REACHED,
        triggerThreshold = 1
    )

    val rankC = Achievement(
        id = "ach_rank_c",
        title = "Mid-Tier Threat",
        description = "Reach Rank C.",
        flavorText = "C-rank is where most hunters plateau. The System is watching to see if you are like most hunters.",
        iconName = "military_tech",
        rarity = Rarity.RARE,
        xpBonus = 350,
        triggerType = AchievementTrigger.RANK_REACHED,
        triggerThreshold = 2
    )

    val rankB = Achievement(
        id = "ach_rank_b",
        title = "The Upper Echelon",
        description = "Reach Rank B.",
        flavorText = "B-rank. The System has never awarded this to a hunter who did not genuinely earn it. You genuinely earned it.",
        iconName = "military_tech",
        rarity = Rarity.EPIC,
        xpBonus = 700,
        triggerType = AchievementTrigger.RANK_REACHED,
        triggerThreshold = 3
    )

    val rankS = Achievement(
        id = "ach_rank_s",
        title = "The One Percent",
        description = "Reach Rank S.",
        flavorText = "The System pauses all other processes to register this. S-rank. You are not common. You are not ordinary. You are what the system was built for.",
        iconName = "workspace_premium",
        rarity = Rarity.LEGENDARY,
        xpBonus = 2000,
        triggerType = AchievementTrigger.RANK_REACHED,
        triggerThreshold = 4
    )

    // ── Meta milestones: boss raids ────────────────────────────────────────────

    val tenBossRaids = Achievement(
        id = "ach_boss_10",
        title = "The Raid Master",
        description = "Complete 10 Boss Raid weeks.",
        flavorText = "Ten bosses defeated across ten weeks of relentless effort. The System has stopped sending easy bosses. You have earned the difficult ones.",
        iconName = "security",
        rarity = Rarity.LEGENDARY,
        xpBonus = 1500,
        triggerType = AchievementTrigger.BOSS_DEFEATED,
        triggerThreshold = 10
    )

    // ── Fun / Hidden achievements ─────────────────────────────────────────────

    val perfectWeekSeven = Achievement(
        id = "ach_perfect_week_7",
        title = "Flawless Seven",
        description = "Complete 7 consecutive days with 100% mission completion.",
        flavorText = "Seven consecutive perfect days. The System has flagged this entry for review. It is not often it sees perfection sustained this long.",
        iconName = "diamond",
        rarity = Rarity.EPIC,
        xpBonus = 800,
        triggerType = AchievementTrigger.PERFECT_WEEK,
        triggerThreshold = 1
    )

    val sixCategoryWeek = Achievement(
        id = "ach_six_category_week",
        title = "The Complete Hunter",
        description = "Complete at least one mission from each of the 6 categories in a single week.",
        flavorText = "Six domains engaged in seven days. The System rarely observes this level of breadth. You are not a specialist. You are a complete weapon.",
        iconName = "category",
        rarity = Rarity.EPIC,
        xpBonus = 500,
        triggerType = AchievementTrigger.MISSIONS_COMPLETED,
        triggerThreshold = 6
    )

    val allExpanded: List<Achievement> = listOf(
        // Physical mastery
        physicalTen, physicalTwentyFive, physicalFifty,
        // Mental mastery
        mentalTen, mentalTwentyFive, mentalFifty,
        // Wellness mastery
        wellnessTen, wellnessTwentyFive, wellnessFifty,
        // Productivity mastery
        productivityTen, productivityTwentyFive, productivityFifty,
        // Social mastery
        socialTen, socialTwentyFive, socialFifty,
        // Creativity mastery
        creativityTen, creativityTwentyFive, creativityFifty,
        // Special behaviours
        earlyBirdMission, sundaySweep, firstBossRaid, threeShadows, midnightMission,
        // Recovery
        comeback, perfectRecovery,
        // XP milestones
        xpFiveHundred, xpTwoThousand, xpFiveThousand,
        // Rank milestones
        rankD, rankC, rankB, rankS,
        // Boss milestones
        tenBossRaids,
        // Fun / hidden
        perfectWeekSeven, sixCategoryWeek
    )
}
