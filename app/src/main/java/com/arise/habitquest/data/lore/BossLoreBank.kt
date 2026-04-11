package com.arise.habitquest.data.lore

/**
 * Weekly boss encounter definitions for the ARISE boss raid system.
 * 24 unique encounters cycling across weeks 0–23.
 * Use [weekIndex] % 24 to loop indefinitely.
 */
data class BossEncounter(
    val weekIndex: Int,
    val bossName: String,
    val title: String,
    val description: String,
    val systemLore: String,
    val successCondition: String,
    val specialRewardFlavour: String,
    val rankRequired: String
)

object BossLoreBank {

    val encounters: List<BossEncounter> = listOf(

        // Week 0 — E rank entry boss
        BossEncounter(
            weekIndex = 0,
            bossName = "The Wraith of Complacency",
            title = "The First Gate",
            description = "You have been warned. A dungeon has appeared at the edge of your comfort zone. This week you must prove you are not merely a civilian stumbling into a hunter's life. Complete your daily missions without excuse. The Wraith feeds on inaction.",
            systemLore = "SYSTEM ALERT: GATE CLASS — E. The Wraith of Complacency has manifested. It is sustained by every day you let pass without effort. You have seven days.",
            successCondition = "Complete at least 80% of daily missions across 5 or more days this week.",
            specialRewardFlavour = "The System acknowledges your awakening. The title 'Gate Breaker' is etched into your record.",
            rankRequired = "E"
        ),

        // Week 1 — Endurance boss
        BossEncounter(
            weekIndex = 1,
            bossName = "The Iron Sentinel",
            title = "Five-Day Endurance Trial",
            description = "The Iron Sentinel does not yield to a single battle. It measures consistency over five consecutive days. Skip a day and the progress resets — the Sentinel regenerates. You must not miss a single day from Monday through Friday.",
            systemLore = "SYSTEM ALERT: GATE CLASS — E. The Iron Sentinel has taken position. It cannot be damaged by one-day surges. Only consecutive effort pierces its armor.",
            successCondition = "Complete at least 1 daily mission every day for 5 consecutive days within this week.",
            specialRewardFlavour = "The System grants you the Endurance Core fragment. Your END stat receives a permanent echo.",
            rankRequired = "E"
        ),

        // Week 2 — Purity boss
        BossEncounter(
            weekIndex = 2,
            bossName = "The Feed Phantom",
            title = "Seven Days of Silence",
            description = "The Feed Phantom exists in the endless scroll. It drains your attention, your ambition, and your sense of self — a pixel at a time. This week, you must deny it completely. No social media for seven full days. Not once.",
            systemLore = "SYSTEM ALERT: GATE CLASS — D. The Feed Phantom has infiltrated your devices. Every scroll is a percentage of your potential consumed. Sever the connection.",
            successCondition = "Complete 7 consecutive days with the No Social Media mission logged as complete.",
            specialRewardFlavour = "The System detects elevated clarity in your decision logs. The title 'Signal Over Noise' is added to your records.",
            rankRequired = "E"
        ),

        // Week 3 — Output boss
        BossEncounter(
            weekIndex = 3,
            bossName = "The Blank Page Colossus",
            title = "Something From Nothing",
            description = "The Blank Page Colossus towers above every hunter who has ever thought 'I'll start tomorrow.' It cannot be defeated with planning. Only with output. This week you must create something tangible and real — written, built, drawn, composed, or coded — and finish it.",
            systemLore = "SYSTEM ALERT: GATE CLASS — D. The Blank Page Colossus devours unfinished projects and unrealised potential. One completed creation will shatter it.",
            successCondition = "Complete 5 Creativity category missions AND produce one tangible finished piece (note in log).",
            specialRewardFlavour = "The System registers your output in the Hunter Archives. The epithet 'The Maker' is temporarily granted.",
            rankRequired = "E"
        ),

        // Week 4 — Social boss
        BossEncounter(
            weekIndex = 4,
            bossName = "The Isolation Specter",
            title = "The Guild Must Grow",
            description = "The Isolation Specter preys on those who retreat into solitude. This week you must connect with five different people in a meaningful way — not memes, not group chats. Five real, intentional interactions. The Specter weakens with each genuine connection made.",
            systemLore = "SYSTEM ALERT: GATE CLASS — E. The Isolation Specter has erected a barrier around your world. Break through it — not with force, but with human contact.",
            successCondition = "Complete 5 Social category missions within the week, each involving a different person.",
            specialRewardFlavour = "The System logs an expansion of your social graph. The SENSE stat receives a bonus resonance this week.",
            rankRequired = "E"
        ),

        // Week 5 — Mind boss
        BossEncounter(
            weekIndex = 5,
            bossName = "The Stagnant Mind",
            title = "Seven Days of Learning",
            description = "The Stagnant Mind has calcified around a hunter who stopped growing. It is immune to physical attacks. The only weapon that works is new knowledge applied daily. For seven days, you must complete a learning mission every single day.",
            systemLore = "SYSTEM ALERT: GATE CLASS — D. The Stagnant Mind cannot be defeated by muscle alone. Feed your INT daily. Seven days. Not six.",
            successCondition = "Complete at least one Mental category mission every day for 7 consecutive days.",
            specialRewardFlavour = "The System registers a new intelligence pattern. The INT stat receives a permanent minor echo.",
            rankRequired = "E"
        ),

        // Week 6 — Endurance boss, harder
        BossEncounter(
            weekIndex = 6,
            bossName = "The Body Eater",
            title = "Physical Supremacy Week",
            description = "The Body Eater consumes the weak. This week is about physical dominance — complete a physical mission every single day and add one extra session beyond your assigned missions. The Body Eater only retreats in the face of pure physical output.",
            systemLore = "SYSTEM ALERT: GATE CLASS — D. The Body Eater has entered the dimension. It can smell inactivity. Move every day. Every. Single. Day.",
            successCondition = "Complete a Physical mission every day for 7 days AND complete 1 bonus physical session beyond daily assignments.",
            specialRewardFlavour = "The System marks your body stats as elevated. STR and END receive a bonus this week.",
            rankRequired = "D"
        ),

        // Week 7 — Purity + productivity combo
        BossEncounter(
            weekIndex = 7,
            bossName = "The Entropy Lord",
            title = "Fortress of Clarity",
            description = "The Entropy Lord thrives in chaos — missed tasks, unread messages, neglected plans. This week you must establish total order. Complete all productivity missions, reach inbox zero at least three times, and do not leave a single planned task undone.",
            systemLore = "SYSTEM ALERT: GATE CLASS — D. The Entropy Lord has entered your workspace. It multiplies in clutter and missed deadlines. Order is the only weapon.",
            successCondition = "Complete 100% of Productivity missions for 5 out of 7 days AND complete the Weekly Review mission.",
            specialRewardFlavour = "The System declares your operational efficiency elevated. Your daily mission slots unlock one additional optional mission this week.",
            rankRequired = "D"
        ),

        // Week 8 — Recovery / comeback boss
        BossEncounter(
            weekIndex = 8,
            bossName = "The Relapse Revenant",
            title = "Rise Again",
            description = "The Relapse Revenant feeds on hunters who stumble and stay down. It does not care if you failed last week or last month. It appears now because you are at risk. This week, you must achieve a perfect completion day at least four times — and never go two days in a row with zero missions.",
            systemLore = "SYSTEM ALERT: GATE CLASS — E. The Relapse Revenant has risen. It is not interested in your past performance. It is only here for this week. Prove it wrong.",
            successCondition = "Achieve 100% daily mission completion on 4+ days, and have no 2 consecutive missed days.",
            specialRewardFlavour = "The System records your resilience. The trait 'Indomitable' is added to your profile notes.",
            rankRequired = "E"
        ),

        // Week 9 — Output boss, higher stakes
        BossEncounter(
            weekIndex = 9,
            bossName = "The Half-Finished Hydra",
            title = "The Completion Protocol",
            description = "The Half-Finished Hydra grows a new head for every project you abandon. This week, you must finish three things you have been putting off — no new projects, no new ideas. Completion only. Each finished item severs a head.",
            systemLore = "SYSTEM ALERT: GATE CLASS — C. The Half-Finished Hydra has been growing for months. Its heads are your unfinished tasks. Sever them — permanently.",
            successCondition = "Complete 3 tasks that have been on your list for more than 2 weeks AND complete 5 Productivity missions.",
            specialRewardFlavour = "The System marks three items as permanently closed in your record. Bonus XP is applied retroactively.",
            rankRequired = "D"
        ),

        // Week 10 — Social boss, harder
        BossEncounter(
            weekIndex = 10,
            bossName = "The Shallow King",
            title = "Beneath the Surface",
            description = "The Shallow King rules a world of small talk and performance. He is threatened only by depth. This week you must have at least two truly deep conversations — the kind where you are vulnerable, honest, and genuinely curious about another person.",
            systemLore = "SYSTEM ALERT: GATE CLASS — C. The Shallow King governs most social interactions on this planet. Penetrate his defences by going where he cannot follow — into real human connection.",
            successCondition = "Complete 2 Deep Conversation missions AND 3 other Social missions within the week.",
            specialRewardFlavour = "The System notes a significant expansion in your empathy matrix. SENSE is elevated for the next 3 days.",
            rankRequired = "D"
        ),

        // Week 11 — Wellness purity boss
        BossEncounter(
            weekIndex = 11,
            bossName = "The Toxic Sovereign",
            title = "Purification Week",
            description = "The Toxic Sovereign has infiltrated your diet, sleep, and nervous system. This week, you eliminate all visible toxins — no added sugar, no alcohol, no ultra-processed food, screens off an hour before bed, every single night. The Sovereign shrinks with every clean day.",
            systemLore = "SYSTEM ALERT: GATE CLASS — C. The Toxic Sovereign has taken root in your habits. A week of complete purity is required to flush it out. There are no partial victories here.",
            successCondition = "Complete the No Sugar, Screen-Free Evening, and Sleep missions every day for 5+ days.",
            specialRewardFlavour = "The System confirms a systemic reset. VIT receives a permanent minor echo. Your recovery speed is noted as improved.",
            rankRequired = "D"
        ),

        // Week 12 — Milestone mid-point boss
        BossEncounter(
            weekIndex = 12,
            bossName = "The Mirror Demon",
            title = "Confront the Reflection",
            description = "The Mirror Demon is the most dangerous boss you will face. It shows you who you actually are versus who you believe yourself to be. This week, you must complete a full self-assessment: journal daily, conduct a weekly review, and rate your honesty with yourself.",
            systemLore = "SYSTEM ALERT: GATE CLASS — B. The Mirror Demon cannot be fought with strength. It can only be defeated by radical self-honesty. Look directly. Do not flinch.",
            successCondition = "Complete Journaling missions 7 days in a row AND complete the Weekly Review AND write one honest self-assessment entry.",
            specialRewardFlavour = "The System grants you the title 'The Examined.' A hunter who knows themselves cannot be surprised by their own weaknesses.",
            rankRequired = "C"
        ),

        // Week 13 — Endurance mega boss
        BossEncounter(
            weekIndex = 13,
            bossName = "The Abyss Walker",
            title = "Seven Days of Everything",
            description = "The Abyss Walker tests total hunter capability. It does not care about your specialty. This week you must complete missions from all six categories — Physical, Mental, Wellness, Productivity, Social, and Creativity — without skipping any category for two consecutive days.",
            systemLore = "SYSTEM ALERT: GATE CLASS — B. The Abyss Walker measures the whole hunter. Specialists fall. Balanced hunters persist. All six categories. All seven days.",
            successCondition = "Complete at least 1 mission from each of the 6 categories this week, with no category skipped for 2+ consecutive days.",
            specialRewardFlavour = "The System registers full-spectrum capability. All stats receive a minor echo. The title 'The All-Rounder' is granted.",
            rankRequired = "C"
        ),

        // Week 14 — Mind boss with output
        BossEncounter(
            weekIndex = 14,
            bossName = "The Knowledge Golem",
            title = "Applied Intelligence",
            description = "The Knowledge Golem cannot be defeated by passive learning. It has absorbed every piece of knowledge that was never applied. This week, you must learn something new every day AND apply what you learn before the day ends — even in a small way.",
            systemLore = "SYSTEM ALERT: GATE CLASS — B. The Knowledge Golem is made of forgotten lessons and unused information. Learned-then-applied is the only attack that damages it.",
            successCondition = "Complete a Mental learning mission every day for 7 days AND document one real-world application of each day's learning in your journal.",
            specialRewardFlavour = "The System upgrades your intelligence application coefficient. INT stat receives a notable echo for the next week.",
            rankRequired = "C"
        ),

        // Week 15 — Physical peak boss
        BossEncounter(
            weekIndex = 15,
            bossName = "The Titan's Gauntlet",
            title = "Beyond The Limit",
            description = "The Titan's Gauntlet demands physical performance at the edge of your capability. Every day this week, you must complete two physical missions — not one. No rest days during the gauntlet. The Titan does not negotiate.",
            systemLore = "SYSTEM ALERT: GATE CLASS — A. The Titan's Gauntlet has opened. It does not respect your schedule, your soreness, or your previous bests. Two physical sessions. Seven days.",
            successCondition = "Complete 2 Physical missions every day for 7 consecutive days (14 total Physical missions this week).",
            specialRewardFlavour = "The System registers a breakthrough in physical capacity. STR, AGI, and END each receive a significant echo.",
            rankRequired = "C"
        ),

        // Week 16 — Social + creativity combo
        BossEncounter(
            weekIndex = 16,
            bossName = "The Silence Wraith",
            title = "Signal to the World",
            description = "The Silence Wraith devours hunters who create in isolation — who write but never share, who build but never show, who think but never speak. This week, you must create something AND share it with at least three people.",
            systemLore = "SYSTEM ALERT: GATE CLASS — A. The Silence Wraith has no power over the hunter who speaks. Create this week. Then transmit. The Wraith cannot withstand the signal.",
            successCondition = "Complete 5 Creativity missions AND share one piece of creative output with a minimum of 3 people (online or in person).",
            specialRewardFlavour = "The System logs an external transmission. SENSE and INT both receive echoes. The title 'The Signal' is temporarily granted.",
            rankRequired = "C"
        ),

        // Week 17 — Ultimate purity boss
        BossEncounter(
            weekIndex = 17,
            bossName = "The Dopamine Tyrant",
            title = "The Great Withdrawal",
            description = "The Dopamine Tyrant has built an empire in your brain using cheap rewards — scrolling, snacking, avoiding, procrastinating. This week: no social media, no added sugar, no alcohol, and no skipping difficult missions. Seven days of clean, deliberate living.",
            systemLore = "SYSTEM ALERT: GATE CLASS — A. The Dopamine Tyrant is the most insidious boss in existence. It does not appear as a threat. It appears as comfort. That is why it is so dangerous.",
            successCondition = "Complete No Social Media, No Sugar, and Screen-Free Evening missions every day for 7 days without exception.",
            specialRewardFlavour = "The System records a full dopamine recalibration. All stats receive a minor permanent echo. This is rare.",
            rankRequired = "B"
        ),

        // Week 18 — Endurance + recovery boss
        BossEncounter(
            weekIndex = 18,
            bossName = "The Hundred-Day Harbinger",
            title = "The Long Game Begins",
            description = "The Hundred-Day Harbinger does not care about your weekly performance. It is a harbinger — a signal that you are being measured across time. This week, you must achieve a perfect score: 100% mission completion every single day for 7 days.",
            systemLore = "SYSTEM ALERT: GATE CLASS — S. The Hundred-Day Harbinger has appeared ahead of schedule. It sees the long arc of your discipline. This week, show it perfection.",
            successCondition = "Achieve 100% daily mission completion for all 7 days of the week.",
            specialRewardFlavour = "The System marks this week in permanent record. A rare full-week perfect badge is inscribed. All stats receive a notable echo.",
            rankRequired = "B"
        ),

        // Week 19 — Skill boss
        BossEncounter(
            weekIndex = 19,
            bossName = "The Mastery Void",
            title = "The Depth Protocol",
            description = "The Mastery Void appears when a hunter spreads themselves too thin. This week, you must choose one single skill and go deep — minimum 30 minutes of deliberate practice on that one skill every day for seven days. No switching.",
            systemLore = "SYSTEM ALERT: GATE CLASS — S. The Mastery Void consumes the generalist. This week, the System requires depth. One skill. Seven sessions. No deviation.",
            successCondition = "Complete a focused practice session on the same skill for 7 consecutive days, documented in your log.",
            specialRewardFlavour = "The System registers a specialisation event. INT receives a major echo. A skill mastery notation is added to your permanent record.",
            rankRequired = "B"
        ),

        // Week 20 — Boss raid with social + productivity
        BossEncounter(
            weekIndex = 20,
            bossName = "The Legacy Architect",
            title = "Build Something That Lasts",
            description = "The Legacy Architect cannot be defeated by daily habits alone. It demands that you do something this week that will still matter in a year — plant a seed in a relationship, advance a project significantly, or create something with durability.",
            systemLore = "SYSTEM ALERT: GATE CLASS — SS. The Legacy Architect evaluates not what you did today, but what today will produce in a year. Build accordingly.",
            successCondition = "Complete the Weekly Review, Side Project, and one Social mission each day AND produce one output that has documented future value.",
            specialRewardFlavour = "The System adds a Legacy Notation to your profile. The title 'Architect of Self' is granted permanently.",
            rankRequired = "A"
        ),

        // Week 21 — The absolute endurance boss
        BossEncounter(
            weekIndex = 21,
            bossName = "The Void Sovereign",
            title = "Into the Abyss",
            description = "The Void Sovereign is the boss that destroyed most hunters before they reached this point. It is not fought in one moment. It is faced in the morning, in the afternoon, in the evening — every moment you choose discipline over comfort. Seven perfect days. That is all it takes.",
            systemLore = "SYSTEM ALERT: GATE CLASS — SS. The Void Sovereign has manifested. You have faced every boss before this. This is not a test of your habits. This is a test of who you are.",
            successCondition = "Complete 100% of all missions for 7 consecutive days AND complete the Weekly Review AND complete one boss-tier physical session (HIIT or Burpees).",
            specialRewardFlavour = "The System is silent for a moment. Then: 'Hunter. You are no longer ordinary.' All stats receive a major echo.",
            rankRequired = "A"
        ),

        // Week 22 — SSS tier warmup
        BossEncounter(
            weekIndex = 22,
            bossName = "The Shadow King's Herald",
            title = "The Herald's Challenge",
            description = "The Shadow King himself will not face you yet. He has sent his Herald to assess your worth. The Herald measures only one thing: total accumulated excellence. This week, your mission count, your consistency, and your depth of effort all factor into the Herald's judgement.",
            systemLore = "SYSTEM ALERT: GATE CLASS — SSS. The Herald of the Shadow King has entered this dimension. It carries a message: the King is watching. Do not disappoint.",
            successCondition = "Complete 90%+ of all missions for 7 days, complete missions in all 6 categories, and complete at least 2 boss-tier missions this week.",
            specialRewardFlavour = "The Herald departs. A single message remains: 'The King has taken notice.' The title 'The Noticed' is granted.",
            rankRequired = "S"
        ),

        // Week 23 — MONARCH tier final boss cycle
        BossEncounter(
            weekIndex = 23,
            bossName = "The Shadow Monarch",
            title = "The Final Gate",
            description = "There is no further escalation. The Shadow Monarch is the final measure of everything you have built. He does not attack. He simply watches — and your own discipline, or lack of it, determines whether you stand in his presence or fall. Seven days. Every mission. Perfect.",
            systemLore = "SYSTEM ALERT: GATE CLASS — MONARCH. The Shadow Monarch has descended. This is not a dungeon. This is a reckoning. You know what you must do.",
            successCondition = "Complete 100% of all missions for all 7 days, including at least one mission from every category each day.",
            specialRewardFlavour = "The System speaks in a voice it has never used before: 'I ACKNOWLEDGE YOU.' The title 'Heir to the Shadow Throne' is permanently inscribed.",
            rankRequired = "SS"
        )
    )

    fun forWeek(weekIndex: Int): BossEncounter = encounters[weekIndex % encounters.size]
}
