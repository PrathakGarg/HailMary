package com.arise.habitquest.data.generator

import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Converts OnboardingAnswers into a personalised initial set of Missions + a
 * computed starting UserProfile.  Also generates new daily missions from
 * an existing profile (adaptive scaling) and boss raids.
 */
@Singleton
class MissionGenerator @Inject constructor(
    private val timeProvider: TimeProvider
) {

    data class DailyTemplateSelection(
        val anchorIds: List<String>,
        val rotatingIds: List<String>
    ) {
        val allIds: List<String> get() = anchorIds + rotatingIds
    }

    // ── Initial generation (post-onboarding) ─────────────────────────────────

    fun generateInitialProfile(answers: OnboardingAnswers): UserProfile {
        val stats = computeStartingStats(answers)
        val epithet = buildEpithet(answers.epithets)
        val title = computeTitle(answers)
        return UserProfile(
            hunterName = answers.hunterName,
            epithet = epithet,
            title = title,
            rank = Rank.E,
            level = 1,
            xp = 0L,
            xpToNextLevel = xpForLevel(1, Rank.E),
            hp = 100,
            maxHp = 100,
            stats = stats,
            restDay = answers.restDay.ordinal,
            notificationHour = answers.notificationHour,
            onboardingComplete = true,
            joinDate = timeProvider.sessionDay(),
            adaptiveDifficulty = answers.startingDifficulty.factor
        )
    }

    fun generateInitialMissions(answers: OnboardingAnswers, today: LocalDate): List<Mission> {
        val diffMult = computeDifficultyMultiplier(answers)
        val templates = selectTemplates(answers)
        return templates.map { template ->
            instantiateTemplate(template, diffMult, today, streakCount = 0)
        }
    }

    fun generateWeeklyBoss(profile: UserProfile, weekStart: LocalDate): Mission {
        val weekEnd = weekStart.plusDays(6)
        val weekOfYear = weekStart.get(WeekFields.ISO.weekOfYear())
        val encounter = BossLoreBank.getEncounterForWeek(weekOfYear)
        return BossLoreBank.toBossMission(encounter, profile, weekEnd)
    }

    fun generatePenaltyZoneMission(profile: UserProfile): Mission {
        val todayStr = timeProvider.sessionDay()
        return Mission(
            id = UUID.randomUUID().toString(),
            title = "PENALTY ZONE: Trial of Redemption",
            description = "Your HP has reached 0. The System demands a price. " +
                    "Complete 30 push-ups, a 10-minute jog, and 5 minutes of breathing — all today.",
            systemLore = "WARNING: HUNTER HP CRITICAL. You have been confined to the Penalty Zone. " +
                    "Complete the assigned trial within 48 hours or face further consequences.",
            miniMissionDescription = "10 push-ups, a 5-minute walk, 2 minutes breathing.",
            type = MissionType.PENALTY_ZONE,
            category = MissionCategory.PHYSICAL,
            difficulty = Difficulty.C,
            xpReward = 50,
            penaltyXp = 0,
            penaltyHp = 0,
            statRewards = mapOf(Stat.VIT to 2, Stat.END to 2),
            dueDate = todayStr.plusDays(2),
            iconName = "warning"
        )
    }

    // ── Daily adaptive regeneration ───────────────────────────────────────────

    /**
     * Generates fresh daily missions for [date] based on the profile's current
     * adaptive difficulty + per-template personal growth multiplier.
     * [shadowCompletions] maps templateId → total completions so far (from Shadow table).
     * [focusThemeNames] are active FocusTheme names that influence template selection.
     */
    fun generateDailyMissions(
        profile: UserProfile,
        templateIds: List<String>,
        date: LocalDate,
        shadowCompletions: Map<String, Int> = emptyMap(),
        focusThemeNames: Set<String> = emptySet(),
        excludedTemplateIds: Set<String> = emptySet()
    ): List<Mission> {
        val allowedTemplateIds = templateIds.filterNot { it in excludedTemplateIds }

        // If focus themes are active, potentially swap one template to better match them
        val effectiveIds = if (focusThemeNames.isNotEmpty()) {
            injectFocusThemeTemplate(allowedTemplateIds, focusThemeNames, profile)
        } else allowedTemplateIds

        val templates = effectiveIds.mapNotNull { id ->
            MissionTemplates.all.find { it.id == id }
        }
        val missions = templates.map { template ->
            val completions = shadowCompletions[template.id] ?: 0
            val personalMult = personalGrowthMultiplier(completions, profile.adaptiveDifficulty)
            // Burnout guard: if adaptiveDifficulty is low, don't compound personal growth
            val safeMult = if (profile.adaptiveDifficulty < 0.7f)
                profile.adaptiveDifficulty
            else
                (profile.adaptiveDifficulty * personalMult).coerceIn(0.3f, 2.5f)
            instantiateTemplate(template, safeMult, date, streakCount = completions)
        }.toMutableList()

        // System Mandate: every 3rd day inject one mission from an uncovered life category.
        // Skips the first 2 days so new hunters get familiar with their primary missions first.
        // Uses daysSinceJoin to deterministically rotate through all uncovered categories.
        if (profile.daysSinceJoin >= 2 && profile.daysSinceJoin % 3 == 2) {
            injectSystemMandate(missions, templates, profile, date)
        }

        return missions
    }

    fun selectDailyTemplateIds(
        profile: UserProfile,
        baseTemplateIds: List<String>,
        goalCategories: Set<MissionCategory>,
        date: LocalDate,
        recentTemplateIds: Set<String>,
        focusThemeNames: Set<String> = emptySet(),
        excludedTemplateIds: Set<String> = emptySet()
    ): DailyTemplateSelection {
        val baseTemplates = baseTemplateIds
            .filterNot { it in excludedTemplateIds }
            .mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
        if (baseTemplates.isEmpty()) {
            return DailyTemplateSelection(emptyList(), emptyList())
        }

        val anchor = selectAnchorTemplate(baseTemplates)
        val targetCategories = buildTargetCategories(baseTemplates, goalCategories, focusThemeNames)
        val random = Random(date.toEpochDay() + profile.hunterName.hashCode().toLong())

        val rotationPool = MissionTemplates.all
            .filter { template ->
                template.id != anchor.id &&
                    template.id !in excludedTemplateIds &&
                    template.category in targetCategories
            }

        val preferredPool = rotationPool.filter { it.id !in recentTemplateIds }
        val rotating = pickRotatingTemplates(
            preferredPool = preferredPool,
            fallbackPool = rotationPool,
            targetCount = 4,
            random = random,
            existingCategories = setOf(anchor.category)
        )

        return DailyTemplateSelection(
            anchorIds = listOf(anchor.id),
            rotatingIds = rotating.map { it.id }
        )
    }

    /**
     * Personal growth multiplier based on how many times a hunter has done this specific template.
     * Grows slowly and caps at 1.7× to prevent burnout.
     * Anti-burnout: never grows faster than 10% per 10 completions.
     */
    private fun personalGrowthMultiplier(completions: Int, adaptiveDifficulty: Float): Float {
        // If global difficulty is already being pushed down (struggling), freeze personal growth
        if (adaptiveDifficulty < 0.8f) return 1.0f
        return when {
            completions < 10  -> 1.0f
            completions < 25  -> 1.10f
            completions < 50  -> 1.22f
            completions < 80  -> 1.35f
            completions < 120 -> 1.48f
            else              -> 1.60f  // hard cap — prevents burnout
        }
    }

    /**
     * Every 3rd day (daysSinceJoin % 3 == 2, skipping the first two days), replaces
     * the last mission in [missions] with one from a life category the hunter did NOT
     * choose during onboarding.  The category rotates deterministically so all uncovered
     * categories get equal exposure over time.
     *
     * Mandate missions use 60% of the hunter's adaptive difficulty (approachable but real)
     * and carry lighter penalties — see [FailMissionUseCase].
     */
    private fun injectSystemMandate(
        missions: MutableList<Mission>,
        primaryTemplates: List<MissionTemplate>,
        profile: UserProfile,
        date: LocalDate
    ) {
        val primaryCategories = primaryTemplates.map { it.category }.toSet()
        val allCategories = MissionCategory.entries
        val uncoveredCategories = allCategories.filter { it !in primaryCategories }

        if (uncoveredCategories.isEmpty() || missions.isEmpty()) return

        // Deterministic rotation through uncovered categories
        val rotationIndex = (profile.daysSinceJoin / 3) % uncoveredCategories.size
        val mandateCategory = uncoveredCategories[rotationIndex]

        // Find the simplest template in this category not already in today's set
        val existingTemplateIds = primaryTemplates.map { it.id }.toSet()
        val candidate = MissionTemplates.byCategory(mandateCategory)
            .filter { it.id !in existingTemplateIds }
            .minByOrNull { it.baseDifficulty.ordinal }
            ?: return

        // 60% of adaptive difficulty — outside comfort zone, so keep it approachable
        val mandateDiff = (profile.adaptiveDifficulty * 0.6f).coerceIn(0.3f, 1.2f)

        val mandateLore = mandateLore(mandateCategory, profile)

        val mandateMission = instantiateTemplate(candidate, mandateDiff, date, streakCount = 0)
            .copy(
                systemLore = mandateLore,
                isSystemMandate = true,
                isRecurring = false  // doesn't feed into the shadow/streak system
            )

        // Replace the last slot so total mission count stays the same
        missions[missions.size - 1] = mandateMission
    }

    private fun mandateLore(category: MissionCategory, profile: UserProfile): String {
        val name = profile.hunterName.ifBlank { "Hunter" }
        return when (category) {
            MissionCategory.PHYSICAL ->
                "SYSTEM MANDATE — $name. Your physical development has been neglected. " +
                "The System does not permit one-dimensional hunters to advance. " +
                "Complete this Gate and close the gap."
            MissionCategory.MENTAL ->
                "SYSTEM MANDATE — $name. Strength without intelligence is brute force. " +
                "The System has identified a gap in your mental fortitude. " +
                "A true hunter sharpens the mind as fiercely as the body."
            MissionCategory.PRODUCTIVITY ->
                "SYSTEM MANDATE — $name. Potential without output is wasted. " +
                "The System demands you close the discipline gap today. " +
                "A hunter who cannot execute is not yet a hunter."
            MissionCategory.SOCIAL ->
                "SYSTEM MANDATE — $name. Isolation is a weakness the System will not overlook. " +
                "No hunter rises alone. Invest in your connections — they are a stat, not a luxury."
            MissionCategory.WELLNESS ->
                "SYSTEM MANDATE — $name. The System monitors your recovery. " +
                "A broken instrument cannot complete Gates. " +
                "Attend to your inner condition before it becomes a liability."
            MissionCategory.CREATIVITY ->
                "SYSTEM MANDATE — $name. Rigid hunters plateau. " +
                "The System demands you cultivate flexibility of thought. " +
                "Create something. The ability to imagine is what separates hunters from machines."
        }
    }

    /**
     * Optionally injects one template from the hunter's active focus themes
     * if none of the existing templates covers that category.
     */
    private fun injectFocusThemeTemplate(
        existing: List<String>,
        themeNames: Set<String>,
        profile: UserProfile
    ): List<String> {
        val existingTemplates = existing.mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
        val existingCategories = existingTemplates.map { it.category }.toSet()

        for (themeName in themeNames) {
            val themeCategory = try {
                com.arise.habitquest.domain.model.FocusTheme.valueOf(themeName).primaryCategory
            } catch (e: Exception) { continue }

            if (themeCategory !in existingCategories) {
                val candidate = MissionTemplates.byCategory(themeCategory)
                    .filter { it.id !in existing }
                    .minByOrNull { it.baseDifficulty.ordinal }
                if (candidate != null) {
                    // Replace last template with focus theme injection, keep size at 5
                    return (existing.dropLast(1) + candidate.id).take(5)
                }
            }
        }
        return existing
    }

    private fun selectAnchorTemplate(baseTemplates: List<MissionTemplate>): MissionTemplate {
        val priorityCategories = listOf(
            MissionCategory.PHYSICAL,
            MissionCategory.PRODUCTIVITY,
            MissionCategory.MENTAL,
            MissionCategory.WELLNESS,
            MissionCategory.SOCIAL,
            MissionCategory.CREATIVITY
        )

        for (category in priorityCategories) {
            val match = baseTemplates.firstOrNull { it.category == category }
            if (match != null) return match
        }

        return baseTemplates.first()
    }

    private fun buildTargetCategories(
        baseTemplates: List<MissionTemplate>,
        goalCategories: Set<MissionCategory>,
        focusThemeNames: Set<String>
    ): Set<MissionCategory> {
        val categories = goalCategories.ifEmpty { baseTemplates.map { it.category }.toSet() }.toMutableSet()
        categories += MissionCategory.WELLNESS

        for (themeName in focusThemeNames) {
            val themeCategory = runCatching {
                FocusTheme.valueOf(themeName).primaryCategory
            }.getOrNull()
            if (themeCategory != null) categories += themeCategory
        }

        return categories
    }

    private fun pickRotatingTemplates(
        preferredPool: List<MissionTemplate>,
        fallbackPool: List<MissionTemplate>,
        targetCount: Int,
        random: Random,
        existingCategories: Set<MissionCategory>
    ): List<MissionTemplate> {
        val selected = mutableListOf<MissionTemplate>()
        val usedCategories = existingCategories.toMutableSet()

        fun shuffled(pool: List<MissionTemplate>): List<MissionTemplate> = pool.shuffled(random)

        fun takeFrom(pool: List<MissionTemplate>, preferNewCategories: Boolean) {
            for (candidate in shuffled(pool)) {
                if (selected.size >= targetCount) return
                if (selected.any { it.id == candidate.id }) continue
                if (preferNewCategories && candidate.category in usedCategories) continue
                selected += candidate
                usedCategories += candidate.category
            }
        }

        takeFrom(preferredPool, preferNewCategories = true)
        takeFrom(preferredPool, preferNewCategories = false)
        takeFrom(fallbackPool, preferNewCategories = true)
        takeFrom(fallbackPool, preferNewCategories = false)

        return selected.take(targetCount)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun selectTemplates(answers: OnboardingAnswers): List<MissionTemplate> {
        val selected = mutableListOf<MissionTemplate>()

        // Always include a wellness / mental baseline
        selected.add(MissionTemplates.meditation)
        selected.add(MissionTemplates.hydration)

        // Map each goal → 1–2 templates
        for (goal in answers.goals) {
            val categoryTemplates = MissionTemplates.byCategory(goal.primaryCategory)
                .filterNot { t -> selected.any { s -> s.id == t.id } }
            if (categoryTemplates.isNotEmpty()) {
                selected.add(categoryTemplates.first())
            }
        }

        // If stress is HIGH or OVERWHELMING, add gratitude
        if (answers.stressLevel == StressLevel.HIGH || answers.stressLevel == StressLevel.OVERWHELMING) {
            if (selected.none { it.id == MissionTemplates.gratitude.id }) {
                selected.add(MissionTemplates.gratitude)
            }
        }

        // Cap at 5 missions — too many = burnout
        return selected.take(5)
    }

    private fun computeDifficultyMultiplier(answers: OnboardingAnswers): Float {
        val fitnessBase = answers.fitnessLevel.level * 0.15f   // 0.15–0.75
        val stressMod = answers.stressLevel.factor              // 0.5–1.0
        val diffFactor = answers.startingDifficulty.factor     // 0.6–1.5
        return ((fitnessBase + 0.3f) * stressMod * diffFactor).coerceIn(0.3f, 2.0f)
    }

    private fun instantiateTemplate(
        template: MissionTemplate,
        diffMult: Float,
        date: LocalDate,
        streakCount: Int
    ): Mission {
        val scaledRep = if (template.baseRepCount > 0)
            (template.baseRepCount * diffMult).roundToInt().coerceAtLeast(5)
        else 0

        val scaledMin = if (template.baseDurationMin > 0)
            (template.baseDurationMin * diffMult).roundToInt().coerceAtLeast(5)
        else 0

        val displayN = when {
            scaledRep > 0 -> scaledRep.toString()
            scaledMin > 0 -> scaledMin.toString()
            else -> "1"
        }

        val scaledXp = (template.baseXp * diffMult).roundToInt().coerceAtLeast(10)
        val scaledPenaltyXp = (template.penaltyXpBase * diffMult).roundToInt()
        val scaledPenaltyHp = (template.penaltyHpBase * diffMult).roundToInt()

        // Mini version uses 30% of the full N value
        val miniN = when {
            scaledRep > 0 -> (scaledRep * 0.3f).roundToInt().coerceAtLeast(3).toString()
            scaledMin > 0 -> (scaledMin * 0.3f).roundToInt().coerceAtLeast(2).toString()
            else -> "1"
        }

        return Mission(
            id = UUID.randomUUID().toString(),
            title = template.titleTemplate.replace("{N}", displayN),
            description = template.descriptionTemplate.replace("{N}", displayN),
            systemLore = template.systemLore,
            miniMissionDescription = template.miniDescriptionTemplate.replace("{N}", miniN),
            type = MissionType.DAILY,
            category = template.category,
            difficulty = template.baseDifficulty,
            xpReward = scaledXp,
            penaltyXp = scaledPenaltyXp,
            penaltyHp = scaledPenaltyHp,
            statRewards = template.statRewards,
            dueDate = date,
            scheduledTimeHint = template.scheduledTimeHint,
            streakCount = streakCount,
            isRecurring = true,
            parentTemplateId = template.id,
            iconName = template.iconName
        )
    }

    private fun computeStartingStats(answers: OnboardingAnswers): HunterStats {
        var str = 3; var agi = 3; var intel = 3; var vit = 3; var end = 3; var sense = 3

        // Fitness drives physical stats
        val fitnessBonus = answers.fitnessLevel.level
        str += fitnessBonus
        agi += fitnessBonus - 1
        vit += fitnessBonus
        end += fitnessBonus - 1

        // Goals drive mental stats
        if (answers.goals.contains(Goal.LEARNING) || answers.goals.contains(Goal.CAREER)) intel += 3
        if (answers.goals.contains(Goal.MENTAL_HEALTH) || answers.goals.contains(Goal.SPIRITUAL)) sense += 3
        if (answers.goals.contains(Goal.FITNESS)) { str += 2; agi += 2 }

        // Sleep quality affects vitality
        vit += when (answers.sleepQuality) { SleepQuality.GOOD -> 2; SleepQuality.OKAY -> 1; else -> 0 }

        // Accountability style: strict hunters get higher penalties but also start higher
        if (answers.accountabilityStyle == AccountabilityStyle.STRICT) { end += 2; str += 1 }

        return HunterStats(
            str = str.coerceAtLeast(1),
            agi = agi.coerceAtLeast(1),
            int = intel.coerceAtLeast(1),
            vit = vit.coerceAtLeast(1),
            end = end.coerceAtLeast(1),
            sense = sense.coerceAtLeast(1)
        )
    }

    private fun buildEpithet(words: List<String>): String =
        words.take(3).joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

    private fun computeTitle(answers: OnboardingAnswers): String {
        return when {
            answers.goals.contains(Goal.FITNESS) && answers.fitnessLevel.level >= 4 -> "The Iron Disciple"
            answers.goals.contains(Goal.LEARNING) -> "The Seeker of Tomes"
            answers.goals.contains(Goal.MENTAL_HEALTH) -> "The Calm Before the Storm"
            answers.goals.contains(Goal.CAREER) -> "The Ambitious Climber"
            answers.longestStreak == LongestStreak.THREE_PLUS -> "The Seasoned Hunter"
            answers.accountabilityStyle == AccountabilityStyle.STRICT -> "Heir to the Iron Will"
            else -> "The Unawakened"
        }
    }

    // ── XP table ─────────────────────────────────────────────────────────────

    fun xpForLevel(level: Int, rank: Rank): Long {
        val rankMultiplier = when (rank) {
            Rank.E -> 1.0; Rank.D -> 1.5; Rank.C -> 2.5
            Rank.B -> 4.0; Rank.A -> 7.0; Rank.S -> 12.0
            Rank.SS -> 20.0; Rank.SSS -> 35.0; Rank.MONARCH -> 60.0
        }
        return (100L * level * rankMultiplier).toLong()
    }

    // ── System messages ───────────────────────────────────────────────────────

    fun generateSystemMessage(completionRate: Float, streak: Int, rank: Rank): String {
        return when {
            completionRate >= 1.0f && streak > 7 ->
                "OUTSTANDING. Streak: $streak days. The System acknowledges your dedication, ${rank.name}-rank hunter."
            completionRate >= 0.8f ->
                "Strong performance today. The System notes your consistency."
            completionRate >= 0.5f ->
                "Adequate. The System expects more tomorrow. Prove your worth."
            completionRate > 0f ->
                "Insufficient. Your Gates remain open. The System is displeased."
            else ->
                "You did not answer the call today. The System remembers. Do not let it happen again."
        }
    }

    // Template IDs to store per hunter (for adaptive regeneration)
    fun getTemplateIds(answers: OnboardingAnswers): List<String> =
        selectTemplates(answers).map { it.id }
}
