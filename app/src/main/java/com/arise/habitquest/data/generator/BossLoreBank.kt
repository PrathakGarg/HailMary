package com.arise.habitquest.data.generator

import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.model.Stat
import com.arise.habitquest.domain.model.UserProfile
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.UUID
import kotlin.math.roundToInt

data class BossEncounter(
    val weekIndex: Int,
    val bossName: String,
    val title: String,
    val description: String,
    val systemLore: String,
    val successCondition: String,
    val specialRewardFlavour: String,
    val rankRequired: String = "E"
)

object BossLoreBank {

    val encounters: List<BossEncounter> = listOf(
        BossEncounter(
            weekIndex = 0,
            bossName = "The Wraith of Inertia",
            title = "WEEKLY RAID: Inertia's Gate",
            description = "This week, the System demands you overcome the most dangerous force in existence — your own stillness. Complete all daily gates without skipping a single one. No grace days. No excuses.",
            systemLore = "A Raid-class anomaly has formed from accumulated stagnation. The Wraith of Inertia feeds on untaken first steps. Defeat it, or be consumed.",
            successCondition = "Complete 100% of daily missions at least 5 out of 7 days this week.",
            specialRewardFlavour = "The Wraith dissolves. In its place, the System notes: 'Momentum acknowledged. Multiplier granted.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 1,
            bossName = "Phantom of the Unread",
            title = "WEEKLY RAID: The Tome Gate",
            description = "Knowledge left ungathered rots into weakness. This week you must read or learn every single day — minimum 20 minutes of deliberate, structured learning per day, no exceptions.",
            systemLore = "The Phantom of the Unread haunts those who collect information but never act on it. Seven days of learning will banish it permanently.",
            successCondition = "Complete a reading or learning mission 7 days in a row.",
            specialRewardFlavour = "The Phantom fades. INT +3 bonus granted by the System. 'Knowledge consumed. Power stored.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 2,
            bossName = "The Architect of Chaos",
            title = "WEEKLY RAID: The Clarity Gate",
            description = "Disorder is the enemy of progress. This week, eliminate all digital clutter: zero inbox once, delete 50 unused apps or files, and plan every day in writing before 9am.",
            systemLore = "The Architect of Chaos rebuilds itself in every cluttered folder, unanswered message, and unplanned hour. Bring order to all three domains, and it cannot survive.",
            successCondition = "Zero inbox achieved, 50 files/apps deleted, and daily planning done 5+ mornings.",
            specialRewardFlavour = "The Architect crumbles. 'Order recognised. System clarity mode: engaged.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 3,
            bossName = "The Silence Leviathan",
            title = "WEEKLY RAID: The Connection Gate",
            description = "Isolation is a slow poison. This week, make genuine human contact every single day — a real conversation, not a message. Minimum 10 minutes of meaningful interaction daily.",
            systemLore = "The Silence Leviathan grows in the space between humans. It cannot survive in the presence of real connection. Seven days of genuine contact will starve it.",
            successCondition = "Meaningful human interaction (10+ min conversation) every day for 7 days.",
            specialRewardFlavour = "The Leviathan retreats. SENSE +3 granted. 'The System notes: isolation barrier broken.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 4,
            bossName = "The Iron Tyrant",
            title = "WEEKLY RAID: The Endurance Gate",
            description = "You have been comfortable for too long. This week, push your physical limits daily — every day must include at least one session that genuinely challenges you. No casual walks. Real effort.",
            systemLore = "The Iron Tyrant rules through the fear of physical discomfort. Face it for seven consecutive days and take his throne.",
            successCondition = "Complete a physical mission every single day this week, including one at maximum effort.",
            specialRewardFlavour = "The Iron Tyrant submits. STR +2, END +2 granted. 'Physical dominance logged.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 5,
            bossName = "The Feed Demon",
            title = "WEEKLY RAID: The Silence Gate",
            description = "The most insidious enemy. This week: no social media. Zero. Not reduced — eliminated. Seven full days without scrolling, posting, or consuming social feeds of any kind.",
            systemLore = "The Feed Demon lives inside every notification, every scroll, every dopamine hit stolen from your potential. Seven days of silence will starve it completely.",
            successCondition = "Zero social media usage for 7 full days.",
            specialRewardFlavour = "The Feed Demon is exorcised. SENSE +4 granted. 'Attention reclaimed. The System is pleased.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 6,
            bossName = "The Dusk Serpent",
            title = "WEEKLY RAID: The Sleep Gate",
            description = "Sleep deprivation is the stealth assassin of hunters. This week, be in bed before 23:00 every single night. Protect your recovery with the same aggression you train with.",
            systemLore = "The Dusk Serpent strikes only at exhausted hunters. Seven nights of disciplined sleep deprive it of its prey entirely.",
            successCondition = "In bed before 23:00 all 7 nights this week.",
            specialRewardFlavour = "The Dusk Serpent dissolves at dawn. VIT +3 granted. 'Recovery protocol: optimised.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 7,
            bossName = "The Blank Canvas Horror",
            title = "WEEKLY RAID: The Creation Gate",
            description = "Creation is the highest form of existence. This week you must make something every day — write, draw, build, compose, design. The output must be tangible. Vague thinking does not count.",
            systemLore = "The Blank Canvas Horror materialises wherever creative potential goes unfulfilled. Produce something real every day this week and deny it form.",
            successCondition = "Create something tangible every day for 7 days — one finished piece by week's end.",
            specialRewardFlavour = "The Horror cannot exist where creation lives. INT +2, SENSE +2 granted. 'Output registered.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 8,
            bossName = "The Vault Sentinel",
            title = "WEEKLY RAID: The Treasury Gate",
            description = "Financial chaos undermines every other system. This week: track every single expense, eliminate one recurring waste, and build or review your financial plan. Money is resources. Resources are power.",
            systemLore = "The Vault Sentinel guards the gate between poverty and abundance. It only yields to hunters who demonstrate disciplined resource management for a full week.",
            successCondition = "Track all expenses daily, cancel one unnecessary subscription, review full budget once.",
            specialRewardFlavour = "The Vault Sentinel steps aside. SENSE +2, INT +1 granted. 'Resource efficiency acknowledged.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 9,
            bossName = "The Tempest of Distraction",
            title = "WEEKLY RAID: The Focus Gate",
            description = "Scattered attention produces scattered results. This week, every day must include a minimum 90-minute uninterrupted deep work session — phone in another room, notifications off, door closed.",
            systemLore = "The Tempest of Distraction cannot breach a truly fortified mind. Ninety minutes of fortress-level focus daily for seven days will dissipate it permanently.",
            successCondition = "90+ minutes of documented deep work every day for 7 days.",
            specialRewardFlavour = "The Tempest stills. INT +3 granted. 'Cognitive fortress status: established.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 10,
            bossName = "The Hydra of Habit",
            title = "WEEKLY RAID: The Consistency Gate",
            description = "The Hydra has infinite heads. Every habit you drop grows two more heads of laziness. This week, complete every single daily mission with no mini-version fallbacks. Full versions only. Seven days.",
            systemLore = "The Hydra of Habit feeds on half-measures and compromises. Full execution for seven consecutive days will sever every one of its heads simultaneously.",
            successCondition = "Complete all missions at full version (no mini fallback) for 7 consecutive days.",
            specialRewardFlavour = "The Hydra's final head falls. All stats +1 granted. 'Full commitment recorded by the System.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 11,
            bossName = "The Mirror Tyrant",
            title = "WEEKLY RAID: The Self-Knowledge Gate",
            description = "The Mirror Tyrant shows you the gap between who you are and who you want to be. This week, journal every single day for at least 15 minutes — honest reflection, real plans, no filter.",
            systemLore = "The Mirror Tyrant is destroyed only by radical self-awareness. Seven days of unfiltered written reflection will shatter the illusion it hides behind.",
            successCondition = "Journal for 15+ minutes every day for 7 days.",
            specialRewardFlavour = "The Mirror Tyrant shatters. SENSE +3 granted. 'Self-knowledge: weaponised.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 12,
            bossName = "The Consumption Golem",
            title = "WEEKLY RAID: The Output Gate",
            description = "You consume endlessly but produce little. This week, for every piece of content you consume, you must produce something in return — a note, a summary, a creation, an application. Input equals output.",
            systemLore = "The Consumption Golem is built entirely from unconverted information. Matching every input with an output starves it of the passive energy it feeds on.",
            successCondition = "For every hour of content consumed, produce at least 15 minutes of documented output.",
            specialRewardFlavour = "The Golem collapses into raw material. INT +2, SENSE +1 granted. 'Conversion rate: acknowledged.'",
            rankRequired = "C"
        ),
        BossEncounter(
            weekIndex = 13,
            bossName = "The Frost Giant of Comfort",
            title = "WEEKLY RAID: The Discomfort Gate",
            description = "Comfort is the gate you never entered. This week, do one thing every day that is outside your comfort zone — a cold shower, a difficult conversation, a public action, something that genuinely scares you.",
            systemLore = "The Frost Giant of Comfort grows with every avoided discomfort. Seven days of deliberate discomfort will melt it from the inside out.",
            successCondition = "One documented discomfort-zone action every day for 7 days.",
            specialRewardFlavour = "The Frost Giant melts. END +2, SENSE +2 granted. 'Fear threshold: recalibrated.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 14,
            bossName = "The Void Weaver",
            title = "WEEKLY RAID: The Presence Gate",
            description = "Your phone has stolen your mind. This week: phone face-down during all meals, no phone first 30 minutes of the day, and no phone last 30 minutes before sleep. Every day. Seven days.",
            systemLore = "The Void Weaver spins its web through every unconscious screen glance. Enforce three phone-free anchors daily and unravel its entire web within a week.",
            successCondition = "All three phone rules maintained every day for 7 days.",
            specialRewardFlavour = "The Void Weaver's web dissolves. SENSE +3 granted. 'Presence: reclaimed.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 15,
            bossName = "The Scorched Earth Titan",
            title = "WEEKLY RAID: The Nutrition Gate",
            description = "Poor fuel produces poor output. This week: no ultra-processed food, no alcohol, minimum 2L water every day, and at least two genuinely nutritious meals. Seven days. Full protocol.",
            systemLore = "The Scorched Earth Titan thrives in the wasteland of junk nutrition. One week of clean fuel will burn the ground it stands on.",
            successCondition = "Clean nutrition protocol (no junk, 2L water, 2 real meals) maintained for 7 days.",
            specialRewardFlavour = "The Titan collapses. VIT +4 granted. 'Biological optimisation: logged.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 16,
            bossName = "The Shadow Architect",
            title = "WEEKLY RAID: The Legacy Gate",
            description = "What are you building that will outlast this week? This week you must make measurable progress on a long-term goal: career, skill, creative project, or relationship. Daily logged sessions only.",
            systemLore = "The Shadow Architect builds monuments to wasted potential. Counter it by building something real — something that exists on day 8 that did not exist on day 1.",
            successCondition = "45+ minutes daily on one long-term project, with documented progress each day.",
            specialRewardFlavour = "The Shadow Architect's monument crumbles. INT +2, END +1 granted. 'Legacy foundation: laid.'",
            rankRequired = "C"
        ),
        BossEncounter(
            weekIndex = 17,
            bossName = "The Thousand-Mile Daemon",
            title = "WEEKLY RAID: The Movement Gate",
            description = "Movement is life. This week, hit 10,000 steps every single day — no exceptions for rain, tiredness, or schedule. Hunters move. That is not a suggestion.",
            systemLore = "The Thousand-Mile Daemon cannot keep pace with a hunter who moves. Ten thousand steps every day for seven days will leave it permanently behind.",
            successCondition = "10,000+ steps documented every day for 7 days.",
            specialRewardFlavour = "The Daemon falls behind. AGI +3, END +1 granted. 'Mobility: systemically upgraded.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 18,
            bossName = "The Iron Maw",
            title = "WEEKLY RAID: The Strength Gate",
            description = "Strength is earned, not given. This week, every day must include a resistance training session — bodyweight, weights, or bands. Minimum 20 minutes of actual resistance. No yoga, no walking.",
            systemLore = "The Iron Maw devours those who neglect their physical power. Seven consecutive days of deliberate resistance training will jam its jaws permanently.",
            successCondition = "20+ minutes of resistance training every day for 7 days.",
            specialRewardFlavour = "The Iron Maw breaks its own teeth. STR +4 granted. 'Structural power: catalogued.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 19,
            bossName = "The Phantom Mentor",
            title = "WEEKLY RAID: The Teaching Gate",
            description = "The best way to learn is to teach. This week, explain or teach something you know to someone else every day — a concept, a skill, a lesson. Write it, say it, post it. Knowledge shared is power multiplied.",
            systemLore = "The Phantom Mentor appears only to those who hoard knowledge selfishly. Share what you know every day this week and the Phantom will have nothing to haunt you with.",
            successCondition = "Teach or explain something substantive to another person every day for 7 days.",
            specialRewardFlavour = "The Phantom becomes your ally. INT +2, SENSE +2 granted. 'Teaching multiplier: activated.'",
            rankRequired = "C"
        ),
        BossEncounter(
            weekIndex = 20,
            bossName = "The Dread Accountant",
            title = "WEEKLY RAID: The Audit Gate",
            description = "Most hunters have never truly audited their own habits. This week, track everything: sleep hours, screen time, water intake, calories, work output. Awareness is the first weapon of optimisation.",
            systemLore = "The Dread Accountant thrives in unmeasured chaos. Seven days of total life-tracking will expose every inefficiency it hides in.",
            successCondition = "Track sleep, screen time, water, and one output metric every day for 7 days.",
            specialRewardFlavour = "The Dread Accountant's ledger is seized. All stats +1 granted. 'Self-measurement: weaponised.'",
            rankRequired = "D"
        ),
        BossEncounter(
            weekIndex = 21,
            bossName = "The Obsidian Wall",
            title = "WEEKLY RAID: The Breakthrough Gate",
            description = "There is one goal you have been avoiding for weeks. You know which one. This week, attack it every day for at least 30 minutes. No excuses. The Obsidian Wall only yields to sustained assault.",
            systemLore = "The Obsidian Wall is built from every postponed priority. It will not negotiate. It does not yield to partial effort. Only daily sustained attack for seven days will break it.",
            successCondition = "30+ minutes daily on your single most-avoided goal, for 7 days.",
            specialRewardFlavour = "The Obsidian Wall shatters. All stats +2 granted. 'The System acknowledges: the hardest gate was faced.'",
            rankRequired = "B"
        ),
        BossEncounter(
            weekIndex = 22,
            bossName = "The Abyssal Mirror",
            title = "WEEKLY RAID: The Gratitude Gate",
            description = "Dissatisfaction is the Abyssal Mirror's power source. This week: write 5 genuine gratitudes every morning before checking your phone, and perform one act of service for someone else every day.",
            systemLore = "The Abyssal Mirror feeds on ingratitude and isolation. Five daily gratitudes and one act of service every day will deny it every source of power it has.",
            successCondition = "5 morning gratitudes + 1 act of service every day for 7 days.",
            specialRewardFlavour = "The Abyssal Mirror goes dark. SENSE +3, END +1 granted. 'Positive feedback loop: initiated.'",
            rankRequired = "E"
        ),
        BossEncounter(
            weekIndex = 23,
            bossName = "The Sovereign of Shadows",
            title = "WEEKLY RAID: The Monarch's Trial",
            description = "This is the hardest week. Every mission completed at full version. No phones before 9am or after 9pm. Daily exercise, daily learning, daily connection. The Sovereign does not yield to anything less than total commitment.",
            systemLore = "The Sovereign of Shadows is the final test before ascension. It was created specifically for those who have reached this point. Prove you belong in the ranks above you.",
            successCondition = "All daily missions completed at full version + all three phone rules + exercise + learning every single day for 7 days.",
            specialRewardFlavour = "The Sovereign kneels. The System speaks: 'You have proven yourself. The next rank recognises your name.'",
            rankRequired = "A"
        )
    )

    fun getEncounterForWeek(weekOfYear: Int): BossEncounter {
        val index = weekOfYear % encounters.size
        return encounters[index]
    }

    fun toBossMission(encounter: BossEncounter, profile: UserProfile, weekEnd: LocalDate): Mission {
        val bossXp = (350 * profile.adaptiveDifficulty).roundToInt()
        return Mission(
            id = UUID.randomUUID().toString(),
            title = encounter.title,
            description = "${encounter.description}\n\n✦ SUCCESS CONDITION: ${encounter.successCondition}",
            systemLore = encounter.systemLore,
            miniMissionDescription = "Complete 50% of daily gates and attempt the core challenge at least 4 days.",
            type = MissionType.BOSS_RAID,
            category = MissionCategory.PHYSICAL,
            difficulty = Difficulty.C,
            xpReward = bossXp,
            penaltyXp = 40,
            penaltyHp = 20,
            statRewards = mapOf(Stat.STR to 1, Stat.INT to 1, Stat.END to 1, Stat.SENSE to 1),
            dueDate = weekEnd,
            progressTarget = 7,
            iconName = "military_tech"
        )
    }
}
