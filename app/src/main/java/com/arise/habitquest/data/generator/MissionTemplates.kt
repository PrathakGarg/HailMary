package com.arise.habitquest.data.generator

import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MuscleRegion
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.PhysicalMissionFamily
import com.arise.habitquest.domain.model.Stat

/**
 * Template blueprint used by MissionGenerator to produce real Mission objects.
 * [durationMin] is the base duration in minutes (scaled by difficulty multiplier).
 * [repCount] is for rep-based exercises (0 = duration-based).
 */
data class MissionTemplate(
    val id: String,
    val titleTemplate: String,   // {N} = scaled number
    val descriptionTemplate: String,
    val miniDescriptionTemplate: String,
    val systemLore: String,
    val category: MissionCategory,
    val baseDifficulty: Difficulty,
    val baseDurationMin: Int = 0,
    val baseRepCount: Int = 0,
    val baseXp: Int = 50,
    val statRewards: Map<Stat, Int> = emptyMap(),
    val penaltyXpBase: Int = 15,
    val penaltyHpBase: Int = 5,
    val iconName: String = "fitness_center",
    val scheduledTimeHint: String? = null,  // MORNING / AFTERNOON / EVENING
    val physicalFamily: PhysicalMissionFamily = PhysicalMissionFamily.UNSPECIFIED,
    val muscleLoad: Map<MuscleRegion, Float> = emptyMap()
)

object MissionTemplates {

    // ── PHYSICAL ─────────────────────────────────────────────────────────────

    val pushUps = MissionTemplate(
        id = "tpl_push_ups",
        titleTemplate = "Iron Fists [{N} Push-Ups]",
        descriptionTemplate = "Complete {N} push-ups in one session. You may rest between sets. Form over speed.",
        miniDescriptionTemplate = "Complete {N} push-ups (any pace, any form).",
        systemLore = "The System detects weakness in your upper body. A Gate has appeared. Prove your strength or be left behind.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseRepCount = 15,
        baseXp = 60,
        statRewards = mapOf(Stat.STR to 1, Stat.VIT to 1),
        penaltyXpBase = 15, penaltyHpBase = 8,
        iconName = "fitness_center", scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.PUSH,
        muscleLoad = mapOf(
            MuscleRegion.CHEST to 0.4f,
            MuscleRegion.TRICEPS to 0.3f,
            MuscleRegion.SHOULDERS to 0.2f,
            MuscleRegion.CORE to 0.1f
        )
    )

    val squat = MissionTemplate(
        id = "tpl_squats",
        titleTemplate = "Steel Legs [{N} Squats]",
        descriptionTemplate = "Complete {N} bodyweight squats. Keep your back straight and go below parallel.",
        miniDescriptionTemplate = "Complete {N} partial squats (wall sits count).",
        systemLore = "Your legs are the foundation of a hunter's power. No foundation — no throne.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseRepCount = 20,
        baseXp = 55,
        statRewards = mapOf(Stat.STR to 1, Stat.AGI to 1),
        penaltyXpBase = 12, penaltyHpBase = 7,
        iconName = "directions_run",
        physicalFamily = PhysicalMissionFamily.LOWER_BODY,
        muscleLoad = mapOf(
            MuscleRegion.QUADS to 0.45f,
            MuscleRegion.GLUTES to 0.3f,
            MuscleRegion.HAMSTRINGS to 0.15f,
            MuscleRegion.CORE to 0.1f
        )
    )

    val morningRun = MissionTemplate(
        id = "tpl_morning_run",
        titleTemplate = "Dawn Patrol [{N}-Minute Run]",
        descriptionTemplate = "Run or brisk-walk continuously for {N} minutes. No stopping. The gate demands movement.",
        miniDescriptionTemplate = "Walk briskly for {N} minutes without stopping.",
        systemLore = "The System activates: ENDURANCE GATE. Every step forward closes the distance between you and your limits.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 80,
        statRewards = mapOf(Stat.AGI to 2, Stat.END to 1),
        penaltyXpBase = 20, penaltyHpBase = 10,
        iconName = "directions_run", scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.CARDIO_NEAT,
        muscleLoad = mapOf(
            MuscleRegion.CARDIO_CONDITIONING to 0.6f,
            MuscleRegion.QUADS to 0.15f,
            MuscleRegion.HAMSTRINGS to 0.15f,
            MuscleRegion.CALVES to 0.1f
        )
    )

    val plank = MissionTemplate(
        id = "tpl_plank",
        titleTemplate = "Core of Steel [{N}-Second Plank]",
        descriptionTemplate = "Hold a plank position for {N} seconds. One attempt, then rest.",
        miniDescriptionTemplate = "Hold a plank for {N} seconds (knee plank allowed).",
        systemLore = "The core is the source of all power. A hunter with no core is a hunter who falls.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.F,
        baseDurationMin = 1,
        baseXp = 45,
        statRewards = mapOf(Stat.STR to 1, Stat.END to 1),
        penaltyXpBase = 10, penaltyHpBase = 5,
        iconName = "fitness_center",
        physicalFamily = PhysicalMissionFamily.CORE_STABILITY,
        muscleLoad = mapOf(
            MuscleRegion.CORE to 0.7f,
            MuscleRegion.SHOULDERS to 0.15f,
            MuscleRegion.GLUTES to 0.15f
        )
    )

    val coldShower = MissionTemplate(
        id = "tpl_cold_shower",
        titleTemplate = "Ice Gate [Cold Shower]",
        descriptionTemplate = "End your shower with at least 60 seconds of cold water. Full body, no cheating.",
        miniDescriptionTemplate = "30 seconds cold water on your arms and face.",
        systemLore = "Only those who embrace discomfort can wield the cold. Enter the Ice Gate.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.D,
        baseXp = 70,
        statRewards = mapOf(Stat.VIT to 1, Stat.SENSE to 1),
        penaltyXpBase = 15, penaltyHpBase = 8,
        iconName = "water_drop", scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.RECOVERY_SUPPORT,
        muscleLoad = mapOf(MuscleRegion.CARDIO_CONDITIONING to 1.0f)
    )

    val stretching = MissionTemplate(
        id = "tpl_stretching",
        titleTemplate = "Limber Hunter [{N}-Minute Stretch]",
        descriptionTemplate = "Perform {N} minutes of full-body stretching. Focus on areas that feel tight.",
        miniDescriptionTemplate = "5 minutes of neck, shoulder, and hip flexor stretches.",
        systemLore = "Flexibility is survival. A stiff hunter breaks. A limber hunter bends.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.F,
        baseDurationMin = 10,
        baseXp = 40,
        statRewards = mapOf(Stat.AGI to 1, Stat.VIT to 1),
        penaltyXpBase = 8, penaltyHpBase = 4,
        iconName = "self_improvement", scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.MOBILITY_PREHAB,
        muscleLoad = mapOf(
            MuscleRegion.SHOULDERS to 0.2f,
            MuscleRegion.UPPER_BACK_LATS to 0.15f,
            MuscleRegion.CORE to 0.1f,
            MuscleRegion.GLUTES to 0.2f,
            MuscleRegion.QUADS to 0.15f,
            MuscleRegion.HAMSTRINGS to 0.2f
        )
    )

    val steps = MissionTemplate(
        id = "tpl_steps",
        titleTemplate = "Field Scout [{N},000 Steps]",
        descriptionTemplate = "Reach {N},000 steps today. Use your phone pedometer or fitness tracker.",
        miniDescriptionTemplate = "Reach {N},000 steps (half the target).",
        systemLore = "The hunter who walks the most paths discovers the most gates.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseXp = 65,
        statRewards = mapOf(Stat.AGI to 1, Stat.END to 1),
        penaltyXpBase = 15, penaltyHpBase = 7,
        iconName = "directions_walk",
        physicalFamily = PhysicalMissionFamily.CARDIO_NEAT,
        muscleLoad = mapOf(
            MuscleRegion.CARDIO_CONDITIONING to 0.55f,
            MuscleRegion.QUADS to 0.2f,
            MuscleRegion.HAMSTRINGS to 0.15f,
            MuscleRegion.CALVES to 0.1f
        )
    )

    // ── MENTAL ────────────────────────────────────────────────────────────────

    val deepWork = MissionTemplate(
        id = "tpl_deep_work",
        titleTemplate = "Focus Dungeon [{N}-Minute Deep Work]",
        descriptionTemplate = "Work on your most important task for {N} minutes with no distractions. Phone face-down, notifications off.",
        miniDescriptionTemplate = "15 minutes of focused work on any meaningful task.",
        systemLore = "Distraction is the enemy of power. The System initiates FOCUS GATE. Prove your mind is a weapon.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 45,
        baseXp = 90,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 20, penaltyHpBase = 10,
        iconName = "psychology", scheduledTimeHint = "MORNING"
    )

    val reading = MissionTemplate(
        id = "tpl_reading",
        titleTemplate = "Tome of Knowledge [{N} Pages]",
        descriptionTemplate = "Read {N} pages of a non-fiction book, educational content, or your field's literature.",
        miniDescriptionTemplate = "Read {N} pages of any book — fiction counts on hard days.",
        systemLore = "Knowledge is the rarest item in any dungeon. Every page consumed is power stored.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseRepCount = 20,
        baseXp = 60,
        statRewards = mapOf(Stat.INT to 2),
        penaltyXpBase = 12, penaltyHpBase = 6,
        iconName = "auto_stories"
    )

    val journaling = MissionTemplate(
        id = "tpl_journaling",
        titleTemplate = "Hunter's Log [{N}-Minute Journal]",
        descriptionTemplate = "Write in your journal for {N} minutes. Reflect on yesterday, plan today, or process emotions.",
        miniDescriptionTemplate = "Write 3 sentences: one thing that went well, one challenge, one intention.",
        systemLore = "The greatest hunters record their battles. Your log is your power map.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 10,
        baseXp = 50,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1),
        penaltyXpBase = 10, penaltyHpBase = 5,
        iconName = "edit_note", scheduledTimeHint = "EVENING"
    )

    val learning = MissionTemplate(
        id = "tpl_online_learning",
        titleTemplate = "Skill Gate [{N}-Minute Course]",
        descriptionTemplate = "Complete {N} minutes of an online course, tutorial, or structured learning session.",
        miniDescriptionTemplate = "Watch or read one lesson from any educational resource.",
        systemLore = "Skills are the true currency of hunters. Each lesson is a stat point earned.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 30,
        baseXp = 75,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 18, penaltyHpBase = 8,
        iconName = "school"
    )

    val noPhone = MissionTemplate(
        id = "tpl_no_phone",
        titleTemplate = "Digital Silence [{N}-Hour No-Phone]",
        descriptionTemplate = "Spend {N} consecutive hours without picking up your phone. No social media, no scrolling.",
        miniDescriptionTemplate = "1 hour without social media (calls/messages allowed).",
        systemLore = "The phone is your greatest enemy. Every scroll steals from your potential. The System demands discipline.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.C,
        baseDurationMin = 180,
        baseXp = 85,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 20, penaltyHpBase = 10,
        iconName = "phone_disabled"
    )

    // ── WELLNESS ──────────────────────────────────────────────────────────────

    val meditation = MissionTemplate(
        id = "tpl_meditation",
        titleTemplate = "Still Mind Gate [{N}-Minute Meditation]",
        descriptionTemplate = "Meditate for {N} minutes. Use an app, guided audio, or sit in silence. Focus on breath.",
        miniDescriptionTemplate = "5 deep breaths with full attention. That's it.",
        systemLore = "The mind untrained is a dungeon with no exit. The System requires stillness before power.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 10,
        baseXp = 55,
        statRewards = mapOf(Stat.SENSE to 2, Stat.END to 1),
        penaltyXpBase = 12, penaltyHpBase = 6,
        iconName = "self_improvement", scheduledTimeHint = "MORNING"
    )

    val sleep = MissionTemplate(
        id = "tpl_sleep",
        titleTemplate = "Recovery Gate [Sleep by {N}:00]",
        descriptionTemplate = "Be in bed with lights off by {N}:00. Mark this complete when you are actually in bed — not before. The System resets at 4:30 AM, so late-night completion is valid.",
        miniDescriptionTemplate = "In bed by {N}:30 (30-min extension allowed). Mark complete when you're in bed.",
        systemLore = "Sleep is not weakness. Sleep is maintenance. A hunter who denies rest becomes a liability.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.E,
        baseXp = 50,
        statRewards = mapOf(Stat.VIT to 2, Stat.END to 1),
        penaltyXpBase = 15, penaltyHpBase = 10,
        iconName = "bedtime", scheduledTimeHint = "EVENING"
    )

    val hydration = MissionTemplate(
        id = "tpl_hydration",
        titleTemplate = "Life Source [{N} Glasses of Water]",
        descriptionTemplate = "Drink {N} full glasses (250ml each) of water today. Track it.",
        miniDescriptionTemplate = "Drink {N} glasses today (lower target).",
        systemLore = "Your body is the vessel for all your power. Neglect it and you lose everything.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.F,
        baseRepCount = 8,
        baseXp = 35,
        statRewards = mapOf(Stat.VIT to 1),
        penaltyXpBase = 8, penaltyHpBase = 4,
        iconName = "water_drop"
    )

    val gratitude = MissionTemplate(
        id = "tpl_gratitude",
        titleTemplate = "Light in Darkness [3 Gratitudes]",
        descriptionTemplate = "Write down or speak aloud 3 specific things you are grateful for today. Be genuine.",
        miniDescriptionTemplate = "Name one thing — out loud — that you are grateful for right now.",
        systemLore = "Darkness clouds the mind of even the strongest hunter. Gratitude is light. Use it.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.F,
        baseXp = 30,
        statRewards = mapOf(Stat.SENSE to 1, Stat.END to 1),
        penaltyXpBase = 6, penaltyHpBase = 3,
        iconName = "favorite", scheduledTimeHint = "EVENING"
    )

    val healthyMeal = MissionTemplate(
        id = "tpl_healthy_meal",
        titleTemplate = "Hunter's Ration [Eat Clean Today]",
        descriptionTemplate = "Eat at least 2 nutritious meals today. Avoid ultra-processed food and excessive sugar.",
        miniDescriptionTemplate = "Eat one meal with vegetables or protein today.",
        systemLore = "Fuel determines performance. Junk fuel → junk hunter. The System is watching your intake.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.E,
        baseXp = 55,
        statRewards = mapOf(Stat.VIT to 2),
        penaltyXpBase = 12, penaltyHpBase = 7,
        iconName = "restaurant"
    )

    // ── PRODUCTIVITY ──────────────────────────────────────────────────────────

    val taskList = MissionTemplate(
        id = "tpl_task_planning",
        titleTemplate = "Tactical Planning [Plan Your Day]",
        descriptionTemplate = "Write today's top 3 priorities before 9am. Check off each one as you complete it.",
        miniDescriptionTemplate = "Write 1 thing you must accomplish today — then do it.",
        systemLore = "A hunter without a plan enters dungeons blind. The System demands strategy.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.E,
        baseXp = 45,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1),
        penaltyXpBase = 10, penaltyHpBase = 5,
        iconName = "task_alt", scheduledTimeHint = "MORNING"
    )

    val emailInbox = MissionTemplate(
        id = "tpl_inbox_zero",
        titleTemplate = "Clear the Debris [Inbox Zero]",
        descriptionTemplate = "Process all emails — respond, archive, or delete until inbox is empty.",
        miniDescriptionTemplate = "Reply to the 3 most important emails in your inbox.",
        systemLore = "Clutter is the dungeon that traps you. Clear it. Now.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.D,
        baseXp = 60,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1),
        penaltyXpBase = 12, penaltyHpBase = 6,
        iconName = "mark_email_read"
    )

    val sideProject = MissionTemplate(
        id = "tpl_side_project",
        titleTemplate = "Forge Session [{N}-Minute Project Work]",
        descriptionTemplate = "Spend {N} focused minutes on your side project, business, or personal goal.",
        miniDescriptionTemplate = "Do one small meaningful action on your project (even 10 minutes counts).",
        systemLore = "Your life's work is a dungeon you must conquer daily. One session at a time.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 60,
        baseXp = 90,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 22, penaltyHpBase = 12,
        iconName = "construction", scheduledTimeHint = "EVENING"
    )

    val financeReview = MissionTemplate(
        id = "tpl_finance_review",
        titleTemplate = "Treasury Report [Review Spending]",
        descriptionTemplate = "Log today's expenses and check your budget. Track at least 3 transactions.",
        miniDescriptionTemplate = "Check your bank balance and note one unnecessary expense.",
        systemLore = "Resources are power. A hunter who wastes gold never clears the final dungeon.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.E,
        baseXp = 55,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1),
        penaltyXpBase = 12, penaltyHpBase = 6,
        iconName = "account_balance_wallet"
    )

    val noSocialMedia = MissionTemplate(
        id = "tpl_no_social_media",
        titleTemplate = "Silence the Noise [Social Media Detox]",
        descriptionTemplate = "No social media for the entire day. Email and messaging are fine.",
        miniDescriptionTemplate = "No social media until after 6pm.",
        systemLore = "The dopamine trap is the weakest hunter's cage. Break it.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.C,
        baseXp = 80,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 20, penaltyHpBase = 10,
        iconName = "block"
    )

    // ── SOCIAL ────────────────────────────────────────────────────────────────

    val reachOut = MissionTemplate(
        id = "tpl_reach_out",
        titleTemplate = "Alliance Request [Contact Someone]",
        descriptionTemplate = "Meaningfully contact someone you care about — call, coffee, or a thoughtful message. No memes.",
        miniDescriptionTemplate = "Send a genuine message to one person you haven't spoken to recently.",
        systemLore = "No hunter rises alone. Your network is your greatest guild asset.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.E,
        baseXp = 50,
        statRewards = mapOf(Stat.SENSE to 1, Stat.END to 1),
        penaltyXpBase = 10, penaltyHpBase = 5,
        iconName = "group"
    )

    val kindness = MissionTemplate(
        id = "tpl_kindness",
        titleTemplate = "Shadow Pact [Random Act of Kindness]",
        descriptionTemplate = "Do one specific, intentional kind thing for someone — not vague, not accidental.",
        miniDescriptionTemplate = "Give a genuine compliment to someone today.",
        systemLore = "The strongest hunters build kingdoms, not just dungeons. Invest in people.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.E,
        baseXp = 45,
        statRewards = mapOf(Stat.SENSE to 2),
        penaltyXpBase = 8, penaltyHpBase = 4,
        iconName = "volunteer_activism"
    )

    // ── CREATIVITY ────────────────────────────────────────────────────────────

    val creative = MissionTemplate(
        id = "tpl_creative_work",
        titleTemplate = "Creation Gate [{N} Minutes Creating]",
        descriptionTemplate = "Spend {N} minutes on a creative pursuit: writing, drawing, music, design, crafting, coding for fun.",
        miniDescriptionTemplate = "10 minutes of any creative work — even a quick sketch counts.",
        systemLore = "Creation is the highest form of power. The System rewards those who build.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 30,
        baseXp = 65,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 2),
        penaltyXpBase = 15, penaltyHpBase = 7,
        iconName = "palette"
    )

    val brainstorm = MissionTemplate(
        id = "tpl_brainstorm",
        titleTemplate = "Idea Dungeon [10 New Ideas]",
        descriptionTemplate = "Write 10 ideas on any topic — business ideas, solutions to problems, creative concepts. Quality not required.",
        miniDescriptionTemplate = "Write 5 ideas on any topic in under 5 minutes.",
        systemLore = "Creativity is a muscle. Ideas beget ideas. The System demands quantity.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.E,
        baseXp = 50,
        statRewards = mapOf(Stat.INT to 2),
        penaltyXpBase = 10, penaltyHpBase = 5,
        iconName = "lightbulb"
    )

    // ── All templates ─────────────────────────────────────────────────────────

    private val core: List<MissionTemplate> = listOf(
        pushUps, squat, morningRun, plank, coldShower, stretching, steps,
        deepWork, reading, journaling, learning, noPhone,
        meditation, sleep, hydration, gratitude, healthyMeal,
        taskList, emailInbox, sideProject, financeReview, noSocialMedia,
        reachOut, kindness,
        creative, brainstorm
    )

    val all: List<MissionTemplate> get() = core + MissionTemplatesExpanded.allExpanded

    fun byCategory(category: MissionCategory): List<MissionTemplate> =
        all.filter { it.category == category }
}
