package com.arise.habitquest.domain.model

import java.time.DayOfWeek

enum class Goal(val displayName: String, val primaryCategory: MissionCategory) {
    FITNESS("Physical Fitness", MissionCategory.PHYSICAL),
    CAREER("Career Growth", MissionCategory.PRODUCTIVITY),
    LEARNING("Learning & Knowledge", MissionCategory.MENTAL),
    CREATIVITY("Creative Expression", MissionCategory.CREATIVITY),
    MENTAL_HEALTH("Mental Wellness", MissionCategory.WELLNESS),
    RELATIONSHIPS("Social Connections", MissionCategory.SOCIAL),
    FINANCIAL("Financial Discipline", MissionCategory.PRODUCTIVITY),
    SPIRITUAL("Spiritual Growth", MissionCategory.WELLNESS)
}

enum class FitnessLevel(val displayName: String, val level: Int) {
    SEDENTARY("Sedentary (office life)", 1),
    LIGHT("Lightly Active", 2),
    MODERATE("Moderately Active", 3),
    ACTIVE("Very Active", 4),
    ATHLETE("Athlete / Intense Training", 5)
}

enum class SleepQuality(val displayName: String) {
    POOR("Poor (under 5h / restless)"),
    OKAY("Okay (5–7h)"),
    GOOD("Good (7–9h, refreshed)")
}

enum class StressLevel(val displayName: String, val factor: Float) {
    LOW("Low — relaxed life", 1.0f),
    MEDIUM("Medium — manageable pressure", 0.9f),
    HIGH("High — often overwhelmed", 0.7f),
    OVERWHELMING("Overwhelming — survival mode", 0.5f)
}

enum class AvailableTime(val displayName: String, val hint: String) {
    MORNING("Morning (before work)", "MORNING"),
    LUNCH("Lunch break", "AFTERNOON"),
    EVENING("Evening (after work)", "EVENING"),
    SCATTERED("Scattered throughout the day", null.toString()),
    FLEXIBLE("Flexible / no fixed schedule", null.toString())
}

enum class FailureResponse(val displayName: String) {
    BOUNCE_BACK("I bounce back quickly"),
    NEED_TIME("I need a day or two to reset"),
    TENDS_TO_QUIT("I tend to give up after failures")
}

enum class AccountabilityStyle(val displayName: String) {
    STRICT("Strict — I want real consequences"),
    BALANCED("Balanced — fair warnings and penalties"),
    GENTLE("Gentle — encourage rather than punish")
}

enum class LongestStreak(val displayName: String, val days: Int) {
    NEVER("Never — I haven't managed any", 0),
    LESS_WEEK("Less than a week", 3),
    ONE_TO_FOUR_WEEKS("1–4 weeks", 14),
    ONE_TO_THREE_MONTHS("1–3 months", 45),
    THREE_PLUS("3+ months — I know what I'm doing", 90)
}

enum class FailureReason(val displayName: String) {
    TOO_BUSY("Too busy / no time"),
    LOST_MOTIVATION("Lost motivation"),
    FORGOT("Forgot / no reminders"),
    TOO_HARD("Goals were too hard"),
    LIFE_EVENTS("Life events / emergencies"),
    NO_PROGRESS("Didn't see visible progress"),
    BURNOUT("Burned out from too much at once")
}

enum class StartingDifficulty(val displayName: String, val factor: Float) {
    RECOMMENDED("Recommended (System chooses)", 1.0f),
    EASY("Easy Start — build momentum", 0.6f),
    NORMAL("Normal — balanced challenge", 1.0f),
    HARD("Hard from Day 1 — no mercy", 1.5f)
}

data class OnboardingAnswers(
    val hunterName: String = "",
    val epithets: List<String> = emptyList(),      // 3 selected words
    val goals: Set<Goal> = emptySet(),             // up to 4
    val fitnessLevel: FitnessLevel = FitnessLevel.SEDENTARY,
    val sleepQuality: SleepQuality = SleepQuality.OKAY,
    val stressLevel: StressLevel = StressLevel.MEDIUM,
    val workHoursPerDay: Int = 8,
    val availableTime: AvailableTime = AvailableTime.EVENING,
    val competitiveStyle: Boolean = true,
    val prefersRoutine: Boolean = true,
    val failureResponse: FailureResponse = FailureResponse.NEED_TIME,
    val accountabilityStyle: AccountabilityStyle = AccountabilityStyle.BALANCED,
    val triedBefore: Boolean = false,
    val longestStreak: LongestStreak = LongestStreak.NEVER,
    val failureReasons: Set<FailureReason> = emptySet(),
    val restDay: DayOfWeek = DayOfWeek.SUNDAY,
    val notificationHour: Int = 8,
    val startingDifficulty: StartingDifficulty = StartingDifficulty.RECOMMENDED
)

// Words users can pick to form their epithet
val EPITHET_WORDS = listOf(
    "Silent", "Fierce", "Relentless", "Patient", "Ambitious",
    "Cunning", "Fearless", "Calm", "Burning", "Iron",
    "Shadow", "Lone", "Unstoppable", "Rising", "Cold",
    "Wandering", "Steel", "Broken", "Awakened", "Hidden",
    "Bold", "Proud", "Scarred", "Hungry", "Ancient"
)
