package com.arise.habitquest.data.generator

import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MuscleRegion
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.PhysicalMissionFamily
import com.arise.habitquest.domain.model.Stat

/**
 * Expanded mission template catalogue — 40 new templates across all six categories.
 * Append the contents of [allExpanded] into MissionTemplates.all to activate.
 */
object MissionTemplatesExpanded {

    // ── PHYSICAL (8) ──────────────────────────────────────────────────────────

    val burpees = MissionTemplate(
        id = "tpl_burpees",
        titleTemplate = "Hellgate Reps [{N} Burpees]",
        descriptionTemplate = "Complete {N} burpees with full extension at the top and chest to floor at the bottom. Rest between sets as needed.",
        miniDescriptionTemplate = "Complete {N} modified burpees — step back instead of jumping if needed.",
        systemLore = "The System has opened a Hellgate. Only full-body output will satisfy it. Drop. Rise. Repeat.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.D,
        baseRepCount = 15,
        baseXp = 80,
        statRewards = mapOf(Stat.STR to 1, Stat.AGI to 1, Stat.END to 1),
        penaltyXpBase = 20,
        penaltyHpBase = 10,
        iconName = "fitness_center",
        scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.FULL_BODY,
        muscleLoad = mapOf(
            MuscleRegion.CARDIO_CONDITIONING to 0.35f,
            MuscleRegion.QUADS to 0.2f,
            MuscleRegion.CHEST to 0.15f,
            MuscleRegion.SHOULDERS to 0.1f,
            MuscleRegion.TRICEPS to 0.1f,
            MuscleRegion.CORE to 0.1f
        )
    )

    val pullUps = MissionTemplate(
        id = "tpl_pull_ups",
        titleTemplate = "Iron Ceiling [{N} Pull-Ups]",
        descriptionTemplate = "Complete {N} full pull-ups — chin over bar, arms fully extended at the bottom. Use a resistance band if needed.",
        miniDescriptionTemplate = "Complete {N} resistance-band pull-ups or inverted rows under a table.",
        systemLore = "To rise above requires you to literally pull yourself up. The bar does not care about excuses.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.D,
        baseRepCount = 8,
        baseXp = 75,
        statRewards = mapOf(Stat.STR to 2, Stat.END to 1),
        penaltyXpBase = 18,
        penaltyHpBase = 9,
        iconName = "fitness_center",
        scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.PULL,
        muscleLoad = mapOf(
            MuscleRegion.UPPER_BACK_LATS to 0.45f,
            MuscleRegion.BICEPS to 0.25f,
            MuscleRegion.SHOULDERS to 0.15f,
            MuscleRegion.CORE to 0.15f
        )
    )

    val yoga = MissionTemplate(
        id = "tpl_yoga",
        titleTemplate = "Serpent's Flow [{N}-Minute Yoga]",
        descriptionTemplate = "Complete a {N}-minute yoga session — any style. Focus on breath, hold each pose fully, and move with intention.",
        miniDescriptionTemplate = "Complete {N} minutes of slow sun salutations. Three rounds is enough.",
        systemLore = "Power without control is destruction. The serpent does not strike without stillness first.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 60,
        statRewards = mapOf(Stat.AGI to 1, Stat.VIT to 1, Stat.SENSE to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "self_improvement",
        scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.MOBILITY_PREHAB,
        muscleLoad = mapOf(
            MuscleRegion.CORE to 0.2f,
            MuscleRegion.SHOULDERS to 0.15f,
            MuscleRegion.UPPER_BACK_LATS to 0.15f,
            MuscleRegion.GLUTES to 0.15f,
            MuscleRegion.QUADS to 0.15f,
            MuscleRegion.HAMSTRINGS to 0.2f
        )
    )

    val jumpRope = MissionTemplate(
        id = "tpl_jump_rope",
        titleTemplate = "Rapid Cadence [{N}-Minute Jump Rope]",
        descriptionTemplate = "Jump rope for {N} continuous minutes. Rest 60 seconds between rounds if needed. Aim for consistent rhythm.",
        miniDescriptionTemplate = "Jump rope for {N} minutes — stop-and-go allowed. Every jump counts.",
        systemLore = "Speed is a weapon. The hunter who cannot move fast cannot survive what is coming.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 10,
        baseXp = 65,
        statRewards = mapOf(Stat.AGI to 2, Stat.END to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "directions_run",
        physicalFamily = PhysicalMissionFamily.CARDIO_NEAT,
        muscleLoad = mapOf(
            MuscleRegion.CARDIO_CONDITIONING to 0.5f,
            MuscleRegion.CALVES to 0.25f,
            MuscleRegion.QUADS to 0.15f,
            MuscleRegion.CORE to 0.1f
        )
    )

    val wallSit = MissionTemplate(
        id = "tpl_wall_sit",
        titleTemplate = "Throne of Pain [{N}-Second Wall Sit]",
        descriptionTemplate = "Hold a wall sit position for {N} seconds — thighs parallel to the floor, back flat against the wall. One attempt.",
        miniDescriptionTemplate = "Hold a wall sit for {N} seconds (slightly above parallel is fine).",
        systemLore = "Pain is information. The System requires you to sit in it. How long can a hunter endure?",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 2,
        baseXp = 55,
        statRewards = mapOf(Stat.STR to 1, Stat.END to 2),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "accessibility_new",
        physicalFamily = PhysicalMissionFamily.LOWER_BODY,
        muscleLoad = mapOf(
            MuscleRegion.QUADS to 0.45f,
            MuscleRegion.GLUTES to 0.25f,
            MuscleRegion.CORE to 0.15f,
            MuscleRegion.HAMSTRINGS to 0.15f
        )
    )

    val hiitSession = MissionTemplate(
        id = "tpl_hiit_session",
        titleTemplate = "Blitz Protocol [{N}-Minute HIIT]",
        descriptionTemplate = "Complete a {N}-minute HIIT session — 40 seconds work, 20 seconds rest. Choose any exercises: sprints, jumping jacks, mountain climbers.",
        miniDescriptionTemplate = "Complete {N} minutes of light interval training — 30 seconds on, 30 seconds rest.",
        systemLore = "A Gate has erupted. You have limited time and maximum threat. Burn everything you have.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 20,
        baseXp = 85,
        statRewards = mapOf(Stat.AGI to 2, Stat.END to 1, Stat.VIT to 1),
        penaltyXpBase = 20,
        penaltyHpBase = 10,
        iconName = "sports_martial_arts",
        scheduledTimeHint = "MORNING",
        physicalFamily = PhysicalMissionFamily.FULL_BODY,
        muscleLoad = mapOf(
            MuscleRegion.CARDIO_CONDITIONING to 0.45f,
            MuscleRegion.QUADS to 0.15f,
            MuscleRegion.GLUTES to 0.1f,
            MuscleRegion.CORE to 0.1f,
            MuscleRegion.CHEST to 0.1f,
            MuscleRegion.SHOULDERS to 0.1f
        )
    )

    val foamRolling = MissionTemplate(
        id = "tpl_foam_rolling",
        titleTemplate = "Tissue Repair [{N}-Minute Foam Roll]",
        descriptionTemplate = "Spend {N} minutes foam rolling your legs, back, and shoulders. Pause on tight spots for 30–60 seconds.",
        miniDescriptionTemplate = "Spend {N} minutes foam rolling just your legs and lower back.",
        systemLore = "Even the mightiest hunters must maintain their vessel. Recovery is not rest — it is strategy.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.F,
        baseDurationMin = 10,
        baseXp = 40,
        statRewards = mapOf(Stat.VIT to 1, Stat.AGI to 1),
        penaltyXpBase = 8,
        penaltyHpBase = 4,
        iconName = "accessibility_new",
        scheduledTimeHint = "EVENING",
        physicalFamily = PhysicalMissionFamily.RECOVERY_SUPPORT,
        muscleLoad = mapOf(
            MuscleRegion.QUADS to 0.2f,
            MuscleRegion.HAMSTRINGS to 0.2f,
            MuscleRegion.GLUTES to 0.2f,
            MuscleRegion.UPPER_BACK_LATS to 0.2f,
            MuscleRegion.SHOULDERS to 0.2f
        )
    )

    val stairClimbing = MissionTemplate(
        id = "tpl_stair_climbing",
        titleTemplate = "Ascension Protocol [{N} Floors]",
        descriptionTemplate = "Climb {N} floors of stairs today — at home, office, or any building. Take the stairs every time you have the option.",
        miniDescriptionTemplate = "Climb {N} floors of stairs at least once in one go.",
        systemLore = "Every floor is a metaphor. The System does not provide elevators for those who seek the summit.",
        category = MissionCategory.PHYSICAL,
        baseDifficulty = Difficulty.E,
        baseRepCount = 10,
        baseXp = 55,
        statRewards = mapOf(Stat.END to 1, Stat.AGI to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "stairs",
        physicalFamily = PhysicalMissionFamily.CARDIO_NEAT,
        muscleLoad = mapOf(
            MuscleRegion.CARDIO_CONDITIONING to 0.4f,
            MuscleRegion.QUADS to 0.25f,
            MuscleRegion.GLUTES to 0.15f,
            MuscleRegion.CALVES to 0.1f,
            MuscleRegion.HAMSTRINGS to 0.1f
        )
    )

    // ── MENTAL (7) ────────────────────────────────────────────────────────────

    val mindfulWalk = MissionTemplate(
        id = "tpl_mindful_walk",
        titleTemplate = "The Hunter's Pace [{N}-Minute Mindful Walk]",
        descriptionTemplate = "Walk for {N} minutes with no phone, no music, no podcast. Observe your surroundings. Notice five things you haven't before.",
        miniDescriptionTemplate = "Walk for {N} minutes outside with no headphones. Just walk.",
        systemLore = "The world is full of information the distracted hunter ignores. Silence your devices. Open your senses.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 55,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 10,
        penaltyHpBase = 5,
        iconName = "directions_walk",
        scheduledTimeHint = "MORNING"
    )

    val visualization = MissionTemplate(
        id = "tpl_visualization",
        titleTemplate = "Mental Dungeon Run [{N}-Minute Visualization]",
        descriptionTemplate = "Spend {N} minutes in vivid mental rehearsal. Close your eyes and walk through your most important goal in complete sensory detail — see it, feel it, execute it.",
        miniDescriptionTemplate = "Spend {N} minutes eyes closed, imagining your best possible day today in detail.",
        systemLore = "Every dungeon was first conquered in the mind. The System demands you see victory before you claim it.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 10,
        baseXp = 50,
        statRewards = mapOf(Stat.SENSE to 1, Stat.INT to 1, Stat.END to 1),
        penaltyXpBase = 10,
        penaltyHpBase = 5,
        iconName = "psychology_alt",
        scheduledTimeHint = "MORNING"
    )

    val languagePractice = MissionTemplate(
        id = "tpl_language_practice",
        titleTemplate = "Foreign Cipher [{N}-Minute Language Study]",
        descriptionTemplate = "Study a language for {N} minutes — Duolingo, Anki, a tutor session, or native media. Active practice only: no passive listening while multitasking.",
        miniDescriptionTemplate = "Complete {N} minutes of language app lessons. Streaks count.",
        systemLore = "Every new language is a new dimension unlocked. The hunter who speaks many tongues fears no foreign dungeon.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 65,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "translate"
    )

    val skillDrilling = MissionTemplate(
        id = "tpl_skill_drilling",
        titleTemplate = "Deliberate Practice [{N}-Minute Skill Drill]",
        descriptionTemplate = "Spend {N} minutes in deliberate practice on a specific hard skill — coding challenges, instrument scales, chess puzzles, math problems. Focus on weak spots.",
        miniDescriptionTemplate = "Spend {N} minutes practicing the one specific aspect of a skill you find hardest.",
        systemLore = "The difference between a hunter and a legend is deliberate repetition of the uncomfortable. Drill the weakness away.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 30,
        baseXp = 75,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 18,
        penaltyHpBase = 8,
        iconName = "school"
    )

    val podcastNotes = MissionTemplate(
        id = "tpl_podcast_notes",
        titleTemplate = "Intelligence Debrief [{N}-Minute Podcast + Notes]",
        descriptionTemplate = "Listen to an educational or thought-provoking podcast for {N} minutes and write at least 5 key takeaways or ideas triggered by it.",
        miniDescriptionTemplate = "Listen to {N} minutes of an educational podcast and write down 2 things you learned.",
        systemLore = "Intelligence consumed without reflection is noise. Write it down. Make it yours. Make it power.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 30,
        baseXp = 60,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "headphones"
    )

    val speedReading = MissionTemplate(
        id = "tpl_speed_reading",
        titleTemplate = "Accelerated Tome [{N}-Minute Speed Reading]",
        descriptionTemplate = "Practice speed reading for {N} minutes using a technique (RSVP app, pointer method, or chunking). Track your words-per-minute and try to beat your last score.",
        miniDescriptionTemplate = "Read a single article as fast as you can in {N} minutes. Summarise it in 3 bullet points.",
        systemLore = "The hunter who reads faster processes the dungeon map before others finish blinking. Speed the mind — the body will follow.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 60,
        statRewards = mapOf(Stat.INT to 2),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "auto_stories"
    )

    val gratitudeLetter = MissionTemplate(
        id = "tpl_gratitude_letter",
        titleTemplate = "Letter of Light [Write a Gratitude Letter]",
        descriptionTemplate = "Write a heartfelt letter (minimum 150 words) to someone who has meaningfully impacted your life. You do not have to send it — but you may.",
        miniDescriptionTemplate = "Write a 3-sentence gratitude note to someone specific in your life. Even if you never send it.",
        systemLore = "Gratitude directed at a person is one of the most powerful forces a hunter can wield. The System rarely speaks of this — but it is true.",
        category = MissionCategory.MENTAL,
        baseDifficulty = Difficulty.E,
        baseXp = 60,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "edit",
        scheduledTimeHint = "EVENING"
    )

    // ── WELLNESS (8) ──────────────────────────────────────────────────────────

    val noSugarDay = MissionTemplate(
        id = "tpl_no_sugar",
        titleTemplate = "Sugar Purge [No Added Sugar Today]",
        descriptionTemplate = "Consume zero added sugar today. No sweets, candy, sodas, or desserts. Natural sugars in fruit are fine. Read labels.",
        miniDescriptionTemplate = "No processed sweets or sugary drinks after noon today.",
        systemLore = "The System has identified sugar as the enemy within. Slow poison. Silent sabotage. Cut it.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.D,
        baseXp = 70,
        statRewards = mapOf(Stat.VIT to 2, Stat.END to 1),
        penaltyXpBase = 18,
        penaltyHpBase = 8,
        iconName = "no_food"
    )

    val mealPrep = MissionTemplate(
        id = "tpl_meal_prep",
        titleTemplate = "Ration Forge [Meal Prep Session]",
        descriptionTemplate = "Prepare at least 3 healthy meals or components (proteins, grains, vegetables) in advance. Store them for the next 2+ days.",
        miniDescriptionTemplate = "Prepare one healthy meal from scratch today — no delivery, no processed convenience food.",
        systemLore = "A hunter who controls their fuel controls their battlefield. Prepare now or suffer later.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.D,
        baseXp = 75,
        statRewards = mapOf(Stat.VIT to 2, Stat.INT to 1),
        penaltyXpBase = 18,
        penaltyHpBase = 8,
        iconName = "restaurant",
        scheduledTimeHint = "MORNING"
    )

    val screenFreeEvening = MissionTemplate(
        id = "tpl_screen_free_evening",
        titleTemplate = "Dark Mode [Screen-Free Evening]",
        descriptionTemplate = "No screens (phone, TV, computer) from 8pm until sleep. Use this time to read, journal, talk, or wind down in silence.",
        miniDescriptionTemplate = "No screens for 60 minutes before bed. That is all.",
        systemLore = "Blue light is the jailer of the modern hunter. The System mandates: shut the screens. Reclaim your night.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.D,
        baseXp = 65,
        statRewards = mapOf(Stat.VIT to 1, Stat.SENSE to 2),
        penaltyXpBase = 15,
        penaltyHpBase = 8,
        iconName = "bedtime",
        scheduledTimeHint = "EVENING"
    )

    val boxBreathing = MissionTemplate(
        id = "tpl_box_breathing",
        titleTemplate = "Breath of Steel [{N} Rounds Box Breathing]",
        descriptionTemplate = "Complete {N} rounds of box breathing: inhale 4 counts, hold 4 counts, exhale 4 counts, hold 4 counts. Focus entirely on each count.",
        miniDescriptionTemplate = "Complete {N} rounds of box breathing. Set a timer and focus.",
        systemLore = "The System has noted your stress levels. Breathing is not weakness — it is the reset command for a hunter's nervous system.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.F,
        baseRepCount = 10,
        baseXp = 35,
        statRewards = mapOf(Stat.SENSE to 1, Stat.END to 1),
        penaltyXpBase = 7,
        penaltyHpBase = 3,
        iconName = "air",
        scheduledTimeHint = "MORNING"
    )

    val natureWalk = MissionTemplate(
        id = "tpl_nature_walk",
        titleTemplate = "Wilds Expedition [{N}-Minute Nature Walk]",
        descriptionTemplate = "Walk in a natural setting — park, trail, or anywhere with trees and sky — for {N} minutes. No headphones. Pay attention to your surroundings.",
        miniDescriptionTemplate = "Go outside and walk for {N} minutes in any space with some greenery.",
        systemLore = "The dungeon is man-made. Nature is older than every gate ever opened. Return to it. It will fortify you.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 55,
        statRewards = mapOf(Stat.VIT to 1, Stat.SENSE to 2),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "park"
    )

    val powerNap = MissionTemplate(
        id = "tpl_power_nap",
        titleTemplate = "Tactical Rest [20-Minute Power Nap]",
        descriptionTemplate = "Take a deliberate 20-minute nap between 1pm–3pm. Set an alarm. Do not exceed 25 minutes. This is maintenance, not laziness.",
        miniDescriptionTemplate = "Lie down in silence for 15 minutes with eyes closed — even if you don't fall asleep.",
        systemLore = "Elite hunters know that recovery is a weapon. The System grants you this window. Use it without guilt.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.F,
        baseDurationMin = 20,
        baseXp = 40,
        statRewards = mapOf(Stat.VIT to 1, Stat.END to 1),
        penaltyXpBase = 8,
        penaltyHpBase = 4,
        iconName = "hotel"
    )

    val selfCareRitual = MissionTemplate(
        id = "tpl_self_care",
        titleTemplate = "Vessel Maintenance [{N}-Minute Self-Care]",
        descriptionTemplate = "Dedicate {N} intentional minutes to personal care — skincare routine, grooming, a long shower with care, or any act of deliberate self-maintenance.",
        miniDescriptionTemplate = "Take {N} minutes to do one thing that makes you feel taken care of — could be as simple as moisturising or ironing your clothes.",
        systemLore = "The hunter who neglects their vessel eventually loses it. Tend to yourself. The System sees everything — including this.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.F,
        baseDurationMin = 15,
        baseXp = 35,
        statRewards = mapOf(Stat.VIT to 1, Stat.SENSE to 1),
        penaltyXpBase = 7,
        penaltyHpBase = 3,
        iconName = "spa",
        scheduledTimeHint = "EVENING"
    )

    val intermittentFast = MissionTemplate(
        id = "tpl_intermittent_fast",
        titleTemplate = "Hunger Protocol [{N}-Hour Fast]",
        descriptionTemplate = "Complete a {N}-hour intermittent fasting window. Drink water and black coffee/tea only during the fast. Track your window.",
        miniDescriptionTemplate = "Skip breakfast and delay your first meal by {N} hours from when you wake up.",
        systemLore = "The body accustomed to constant feeding is a body that cannot adapt. The System initiates HUNGER PROTOCOL. Endure.",
        category = MissionCategory.WELLNESS,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 960,
        baseXp = 70,
        statRewards = mapOf(Stat.END to 2, Stat.VIT to 1),
        penaltyXpBase = 18,
        penaltyHpBase = 8,
        iconName = "timer"
    )

    // ── PRODUCTIVITY (7) ──────────────────────────────────────────────────────

    val weeklyReview = MissionTemplate(
        id = "tpl_weekly_review",
        titleTemplate = "Strategic Debrief [Weekly Review]",
        descriptionTemplate = "Conduct a full weekly review: assess what you completed vs planned, identify 3 wins, 3 areas to improve, and set next week's top 3 priorities.",
        miniDescriptionTemplate = "Write one win from this week, one lesson learned, and one priority for next week.",
        systemLore = "The hunter who does not review their battles repeats their defeats. The System demands a debrief.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 30,
        baseXp = 80,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 20,
        penaltyHpBase = 10,
        iconName = "summarize",
        scheduledTimeHint = "EVENING"
    )

    val twoMinuteRule = MissionTemplate(
        id = "tpl_two_minute_sweep",
        titleTemplate = "Debris Clearance [2-Minute Rule Sweep]",
        descriptionTemplate = "Spend 20 minutes doing only tasks that take 2 minutes or less: reply to messages, file a document, clear a surface, schedule an appointment. No task left unfinished if it takes under 2 minutes.",
        miniDescriptionTemplate = "Do 5 tasks right now that take less than 2 minutes each. No overthinking.",
        systemLore = "Small tasks pile into dungeons. Clear the debris before it traps you. The System measures clutter in missed opportunities.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 55,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "cleaning_services"
    )

    val pomodoroBlock = MissionTemplate(
        id = "tpl_pomodoro_block",
        titleTemplate = "Focus Barrage [{N} Pomodoro Blocks]",
        descriptionTemplate = "Complete {N} Pomodoro cycles: 25 minutes of focused work, 5 minutes of rest. Use a timer. No multitasking during the work interval.",
        miniDescriptionTemplate = "Complete {N} Pomodoro cycle on one specific task. One round. Then assess.",
        systemLore = "Focused bursts outrun sustained distraction every time. The System approves of this protocol.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.D,
        baseRepCount = 4,
        baseXp = 75,
        statRewards = mapOf(Stat.INT to 2, Stat.END to 1),
        penaltyXpBase = 18,
        penaltyHpBase = 8,
        iconName = "timer",
        scheduledTimeHint = "MORNING"
    )

    val sayNoPractice = MissionTemplate(
        id = "tpl_say_no",
        titleTemplate = "Gate Sealed [Decline One Request]",
        descriptionTemplate = "Identify one non-essential commitment, meeting, task, or request you should decline today — and do so clearly and without excessive apology.",
        miniDescriptionTemplate = "Decline, delay, or delegate one thing today that you would have normally said yes to out of habit or guilt.",
        systemLore = "Every yes you give drains mana you cannot recover. The System grants power to those who guard their attention.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.D,
        baseXp = 65,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "do_not_disturb_on"
    )

    val networkingReachOut = MissionTemplate(
        id = "tpl_networking",
        titleTemplate = "Guild Expansion [Reach Out to a Contact]",
        descriptionTemplate = "Contact one person in your professional or creative network — reconnect, ask a meaningful question, share something valuable, or request a call. Make it genuine.",
        miniDescriptionTemplate = "Send a thoughtful one-line message to someone you respect in your field.",
        systemLore = "The lone hunter reaches the C-rank ceiling. The guild-builder reaches the heavens. Expand your network today.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.E,
        baseXp = 60,
        statRewards = mapOf(Stat.SENSE to 1, Stat.INT to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "handshake"
    )

    val learnNewTool = MissionTemplate(
        id = "tpl_learn_tool",
        titleTemplate = "New Weapon Acquired [{N}-Minute Tool Tutorial]",
        descriptionTemplate = "Spend {N} minutes learning a tool, app, or technology you have been putting off — a keyboard shortcut set, a new software, a workflow automation. Get to basic proficiency.",
        miniDescriptionTemplate = "Spend {N} minutes watching or reading one tutorial for a tool you want to learn.",
        systemLore = "Your arsenal expands with every tool mastered. The System has detected an unlearned weapon in your inventory.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 25,
        baseXp = 65,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "build"
    )

    val batchErrands = MissionTemplate(
        id = "tpl_batch_errands",
        titleTemplate = "Logistics Run [Batch All Errands]",
        descriptionTemplate = "Group all your pending errands (groceries, admin tasks, calls, purchases) into a single session today. Complete at least 4 in one go without breaking the batch.",
        miniDescriptionTemplate = "Complete 2 errands you have been delaying in a single uninterrupted block.",
        systemLore = "Context switching destroys efficiency. The System demands you cluster your logistics. Eliminate the drag.",
        category = MissionCategory.PRODUCTIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 60,
        baseXp = 60,
        statRewards = mapOf(Stat.INT to 1, Stat.AGI to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "checklist"
    )

    // ── SOCIAL (5) ────────────────────────────────────────────────────────────

    val deepConversation = MissionTemplate(
        id = "tpl_deep_conversation",
        titleTemplate = "Soul Contract [Have a Deep Conversation]",
        descriptionTemplate = "Have a genuine, meaningful conversation with someone — not small talk, not catching up on logistics. Go beneath the surface: ask about fears, dreams, beliefs, or challenges. Minimum 20 minutes.",
        miniDescriptionTemplate = "Ask one meaningful question to someone today and genuinely listen to the answer.",
        systemLore = "The System has observed your conversations. They lack depth. True connection is a stat multiplier. Go deeper.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 20,
        baseXp = 70,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "forum"
    )

    val qualityTime = MissionTemplate(
        id = "tpl_quality_time",
        titleTemplate = "Present Pact [{N}-Minute Phone-Free Quality Time]",
        descriptionTemplate = "Spend {N} minutes with a friend, family member, or partner with both phones face-down and away. Full presence. No interruptions.",
        miniDescriptionTemplate = "Spend {N} minutes with someone you care about with your phone in another room.",
        systemLore = "The System cannot measure presence with numbers. That is why it is the rarest stat of all. Give yours fully.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 30,
        baseXp = 65,
        statRewards = mapOf(Stat.SENSE to 2, Stat.END to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "people"
    )

    val communityVolunteering = MissionTemplate(
        id = "tpl_volunteer",
        titleTemplate = "For the Realm [Community Volunteering]",
        descriptionTemplate = "Spend at least 60 minutes volunteering in your community — at a food bank, cleanup, mentorship program, or any service. Show up, contribute, leave better than you found it.",
        miniDescriptionTemplate = "Do one small act of service for your community today — help a neighbour, clean a shared space, offer assistance somewhere.",
        systemLore = "The hunter who builds only themselves builds a ceiling. The hunter who builds their community builds a kingdom. Choose.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.D,
        baseDurationMin = 60,
        baseXp = 85,
        statRewards = mapOf(Stat.SENSE to 2, Stat.END to 1, Stat.VIT to 1),
        penaltyXpBase = 20,
        penaltyHpBase = 10,
        iconName = "volunteer_activism"
    )

    val askForHelp = MissionTemplate(
        id = "tpl_ask_for_help",
        titleTemplate = "Vulnerability Gate [Ask for Help]",
        descriptionTemplate = "Identify one area where you are stuck or struggling and ask someone — a mentor, friend, colleague, or expert — for genuine help or advice. Then actually receive it.",
        miniDescriptionTemplate = "Ask one person for one piece of advice on something you are working on.",
        systemLore = "Pride is the dungeon that traps many S-rank hunters in C-rank outcomes. Ask. The System has no shame in it.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.D,
        baseXp = 60,
        statRewards = mapOf(Stat.SENSE to 1, Stat.INT to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "help"
    )

    val writeALetter = MissionTemplate(
        id = "tpl_write_letter",
        titleTemplate = "Words Through Time [Write a Letter]",
        descriptionTemplate = "Write a physical or long-form digital letter to someone — a friend you've lost touch with, a family member, a mentor, or even your future self. Minimum 200 words. Send it or save it.",
        miniDescriptionTemplate = "Write a 3-paragraph letter to someone you care about. Keep it genuine.",
        systemLore = "The written word outlasts the sender. Few things move another person more than receiving a real letter. The System tracks acts of permanence.",
        category = MissionCategory.SOCIAL,
        baseDifficulty = Difficulty.E,
        baseXp = 60,
        statRewards = mapOf(Stat.SENSE to 1, Stat.INT to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "mail",
        scheduledTimeHint = "EVENING"
    )

    // ── CREATIVITY (5) ────────────────────────────────────────────────────────

    val musicPractice = MissionTemplate(
        id = "tpl_music_practice",
        titleTemplate = "Resonance Gate [{N}-Minute Music Practice]",
        descriptionTemplate = "Practice your instrument, produce a beat, write lyrics, or compose for {N} uninterrupted minutes. Focus on a specific technique or piece.",
        miniDescriptionTemplate = "Play, hum, or compose freely for {N} minutes without judgment. Just make sound.",
        systemLore = "Music is the vibration that opens gates no physical key can unlock. The System resonates with those who create sound.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 65,
        statRewards = mapOf(Stat.SENSE to 2, Stat.INT to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "music_note"
    )

    val photographyWalk = MissionTemplate(
        id = "tpl_photography_walk",
        titleTemplate = "Eye of the Hunter [{N}-Minute Photography Walk]",
        descriptionTemplate = "Take a {N}-minute walk with the sole purpose of taking intentional photos. Capture at least 10 images you are genuinely proud of. No filters or editing required.",
        miniDescriptionTemplate = "Go outside and take 5 intentional photos of things that catch your eye today.",
        systemLore = "Vision is a power before it is a skill. The hunter who trains their eye trains their judgment. Go. See. Capture.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 30,
        baseXp = 55,
        statRewards = mapOf(Stat.SENSE to 2, Stat.AGI to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "photo_camera"
    )

    val writingPrompt = MissionTemplate(
        id = "tpl_writing_prompt",
        titleTemplate = "Lore Entry [{N}-Minute Writing Prompt]",
        descriptionTemplate = "Pick a writing prompt (any genre) and write for {N} minutes without stopping. Do not edit while writing. Volume over perfection. Minimum 300 words.",
        miniDescriptionTemplate = "Write for {N} minutes on any topic without stopping to edit. Stream of consciousness is fine.",
        systemLore = "Every story is a dungeon. Every blank page is a gate. The System requires you to enter. Write.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.E,
        baseDurationMin = 20,
        baseXp = 60,
        statRewards = mapOf(Stat.INT to 2, Stat.SENSE to 1),
        penaltyXpBase = 12,
        penaltyHpBase = 6,
        iconName = "edit_note"
    )

    val cookSomethingNew = MissionTemplate(
        id = "tpl_cook_new",
        titleTemplate = "Alchemist's Kitchen [Cook Something New]",
        descriptionTemplate = "Cook a dish you have never made before — use a recipe or improvise. The goal is the process: sourcing ingredients, following steps, creating something edible from raw materials.",
        miniDescriptionTemplate = "Add one ingredient or technique to a meal you normally make that you have never used before.",
        systemLore = "Alchemy is the art of transformation. The kitchen is your first laboratory. The System rewards those who create fuel, not merely consume it.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.E,
        baseXp = 65,
        statRewards = mapOf(Stat.INT to 1, Stat.SENSE to 1, Stat.VIT to 1),
        penaltyXpBase = 15,
        penaltyHpBase = 7,
        iconName = "restaurant"
    )

    val sketchDraw = MissionTemplate(
        id = "tpl_sketch_draw",
        titleTemplate = "Visual Inscription [{N}-Minute Sketch]",
        descriptionTemplate = "Draw or sketch for {N} minutes. Subject is your choice — portrait, object, abstract, or from imagination. Focus on the act, not the result.",
        miniDescriptionTemplate = "Draw anything for {N} minutes with a pen or pencil. Stick figures count. Start.",
        systemLore = "The hand that draws trains the mind that thinks. Creativity expressed visually is an ancient hunter art. Pick up the pen.",
        category = MissionCategory.CREATIVITY,
        baseDifficulty = Difficulty.F,
        baseDurationMin = 15,
        baseXp = 45,
        statRewards = mapOf(Stat.SENSE to 2),
        penaltyXpBase = 8,
        penaltyHpBase = 4,
        iconName = "draw"
    )

    // ── All expanded templates ────────────────────────────────────────────────

    val allExpanded: List<MissionTemplate> = listOf(
        // PHYSICAL
        burpees, pullUps, yoga, jumpRope, wallSit, hiitSession, foamRolling, stairClimbing,
        // MENTAL
        mindfulWalk, visualization, languagePractice, skillDrilling, podcastNotes, speedReading, gratitudeLetter,
        // WELLNESS
        noSugarDay, mealPrep, screenFreeEvening, boxBreathing, natureWalk, powerNap, selfCareRitual, intermittentFast,
        // PRODUCTIVITY
        weeklyReview, twoMinuteRule, pomodoroBlock, sayNoPractice, networkingReachOut, learnNewTool, batchErrands,
        // SOCIAL
        deepConversation, qualityTime, communityVolunteering, askForHelp, writeALetter,
        // CREATIVITY
        musicPractice, photographyWalk, writingPrompt, cookSomethingNew, sketchDraw
    )
}
