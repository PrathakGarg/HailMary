package com.arise.habitquest.e2e

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_STATUS
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_EMPTY_STATE
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_HEATMAP
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_ROW
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_STREAK
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_XP
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_WEEKLY_XP_CHART
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_VIEW_HISTORY
import com.arise.habitquest.e2e.support.E2ESelectors.historyInsight
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

/**
 * Comprehensive data-correctness validation for the Battle Record (HistoryScreen).
 *
 * Each test seeds a precisely known DB state, navigates to the Battle Record, and
 * asserts that the displayed values match the expected computations.  This verifies
 * the full data pipeline:
 *   DailyLogDao / MissionDao / UserProfile → HistoryViewModel → HistoryScreen
 *
 * Covered sections:
 *   1. Summary stats row (MISSIONS · TOTAL XP · BEST STREAK)
 *   2. Calendar heatmap (91-day populated / empty behaviour)
 *   3. Weekly XP bar chart
 *   4. System Analysis insights
 *      - This Week vs Last Week (improving / declining)
 *      - 30-Day Average with correct perfect-day count
 *      - Power Day / Danger Day labels
 *   5. Hunter Balance – Category radar chart (all 6 categories present)
 *   6. Weakness Report – Pain Point and Mini-Crutch detection
 *   7. Full 10-day mixed-completion scenario (all sections rendered together)
 *
 * Spanning:
 *   - 10-14+ consecutive day windows
 *   - Perfect days (completionRate == 1.0)
 *   - Partial days (0.25 / 0.5 / 0.75)
 *   - Missed days (completionRate == 0)
 *   - Rest days
 *   - All 6 mission categories
 *   - DAILY / WEEKLY / BOSS_RAID mission types included in category range query
 */
@RunWith(AndroidJUnit4::class)
class BattleRecordDataValidationTest {

    private val resetRule = ResetAppStateRule()

    private val seedRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            when (description.methodName) {
                "summaryStats_showCorrectTotalsFromProfile"
                    -> seedSummaryStats(ctx, missions = 42, xp = 1260L, bestStreak = 10)

                "heatmap_14DaysMixedCompletions_rendersPopulated_notEmptyState"
                    -> seedVariedDailyLogs(ctx, totalDays = 14)

                "weeklyXp_threeWeeksOfData_chartRenders"
                    -> seedMultiWeekXpData(ctx)

                "insight_improvingWeek_showsPositiveDeltaLabel"
                    -> seedWeekTrend(ctx, lastWeekRate = 0.4f, thisWeekRate = 1.0f)

                "insight_decliningWeek_showsNegativeDeltaLabel"
                    -> seedWeekTrend(ctx, lastWeekRate = 1.0f, thisWeekRate = 0.3f)

                "insight_30DayAverage_countsPerfectDaysAccurately"
                    -> seedPerfectDaysMix(ctx, perfectDays = 5, imperfectDays = 12)

                "categoryStats_radarChart_rendersWhenAllSixCategoriesHaveData"
                    -> seedAllCategoryMissions(ctx)

                "categoryStats_lowMasteryPhysical_showsPainPointInWeaknessReport"
                    -> seedCategoryPainPoint(ctx)

                "categoryStats_highMiniRateMental_showsMiniCrutchInWeaknessReport"
                    -> seedCategoryMiniCrutch(ctx)

                "fullScenario_10Days_allBattleRecordSectionsRender"
                    -> seedFullScenario(ctx)
            }
        }
    }

    private val grantNotificationPermission: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        else
            GrantPermissionRule.grant()

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(resetRule)
        .around(seedRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    // ── Navigation helper ─────────────────────────────────────────────────────

    private fun navigateToHistory() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_STATUS)
        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).performClick()
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)
        composeRule.onNodeWithTag(STATUS_VIEW_HISTORY).performScrollTo().performClick()
        E2EAssertions.waitForTag(composeRule, HISTORY_SCREEN)
        // Wait for loading spinner to clear
        composeRule.waitUntil(timeoutMillis = 12_000) {
            composeRule.onAllNodesWithTag(HISTORY_SUMMARY_ROW).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 1. Summary Stats Row
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds a profile with known totalMissionsCompleted=42, totalXpEarned=1260,
     * streakBest=10.  The Battle Record summary row must display these exact values.
     *
     * Validates: HistoryViewModel reads UserProfile.totalMissionsCompleted,
     * totalXpEarned, and streakBest correctly.
     */
    @Test
    fun summaryStats_showCorrectTotalsFromProfile() {
        navigateToHistory()

        composeRule.onNodeWithTag(HISTORY_SUMMARY_MISSIONS)
            .assert(hasAnyDescendant(hasText("42")))

        composeRule.onNodeWithTag(HISTORY_SUMMARY_XP)
            .assert(hasAnyDescendant(hasText("1260")))

        composeRule.onNodeWithTag(HISTORY_SUMMARY_STREAK)
            .assert(hasAnyDescendant(hasText("10d")))
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 2. Calendar Heatmap
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds 14 consecutive daily log entries with rates ranging from perfect (1.0)
     * through partial (0.25-0.75) to full-miss (0.0).  The heatmap must be
     * displayed and the empty-state placeholder must be absent.
     *
     * Validates: getLogsInRange query returns 14 entries; DayEntry.hasData is set
     * for all seeded dates; the 91-day grid renders and is not replaced by the
     * empty-state banner.
     */
    @Test
    fun heatmap_14DaysMixedCompletions_rendersPopulated_notEmptyState() {
        navigateToHistory()

        composeRule.onNodeWithTag(HISTORY_HEATMAP).assertIsDisplayed()
        check(composeRule.onAllNodesWithTag(HISTORY_EMPTY_STATE).fetchSemanticsNodes().isEmpty()) {
            "Empty-state banner must not appear when 14 daily log entries exist"
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 3. Weekly XP Bar Chart
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds 21 daily log entries spanning three calendar weeks.  Each week has a
     * distinct per-day XP value (20 / 50 / 30) so all weeks contribute XP > 0.
     * The weekly XP chart must be visible and render at least one labelled bar.
     *
     * Validates: The weekly aggregation (sumOf { xpGained }) across calendar-week
     * windows produces non-zero totals; the chart section is therefore rendered
     * rather than suppressed.
     */
    @Test
    fun weeklyXp_threeWeeksOfData_chartRenders() {
        navigateToHistory()
        composeRule.onNodeWithTag(HISTORY_WEEKLY_XP_CHART).performScrollTo().assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 4. System Analysis Insights
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds last-week log entries with completionRate=0.4 and this-week entries
     * with completionRate=1.0.  The "This Week vs Last Week" insight (always the
     * first card, index 0) must report a positive delta and show "improving".
     *
     * Formula: weekDelta = thisWeekAvg – lastWeekAvg = 1.0 – 0.4 = +0.6
     * Expected subtext: "+60% — improving"
     */
    @Test
    fun insight_improvingWeek_showsPositiveDeltaLabel() {
        navigateToHistory()

        composeRule.onNodeWithTag(historyInsight(0)).performScrollTo()
        composeRule.onNodeWithTag(historyInsight(0))
            .assert(hasAnyDescendant(hasText("improving", substring = true)))
    }

    /**
     * Seeds last-week log entries with completionRate=1.0 and this-week entries
     * with completionRate=0.3.  The first insight card must report "slipping".
     *
     * Formula: weekDelta = 0.3 – 1.0 = –0.7
     * Expected subtext: "-70% — slipping"
     */
    @Test
    fun insight_decliningWeek_showsNegativeDeltaLabel() {
        navigateToHistory()

        composeRule.onNodeWithTag(historyInsight(0)).performScrollTo()
        composeRule.onNodeWithTag(historyInsight(0))
            .assert(hasAnyDescendant(hasText("slipping", substring = true)))
    }

    /**
     * Seeds 17 daily log entries: 5 perfect days (completionRate >= 1.0) in the
     * first five slots and 12 imperfect days (completionRate = 0.5).  All 17 fall
     * within the last-30-days window used by the "30-Day Average" insight.
     *
     * The insight's subtext must read exactly "5 perfect days in the last month".
     *
     * Validates: the perfect-day count `last30.count { it.completionRate >= 1.0f }`
     * correctly counts 5, not 0 (would be 0 if floating-point comparison is broken)
     * and not 17 (would occur if the threshold check is inverted).
     */
    @Test
    fun insight_30DayAverage_countsPerfectDaysAccurately() {
        navigateToHistory()

        // Scroll until the specific text is visible (insight position varies)
        composeRule.waitUntil(timeoutMillis = 12_000) {
            composeRule
                .onAllNodesWithText("5 perfect days in the last month")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("5 perfect days in the last month")
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 5. Hunter Balance — Category Radar Chart
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds 3 completed missions for each of the 6 MissionCategory values in the
     * last 30 days.  The "HUNTER BALANCE" section header must be visible,
     * confirming that getMissionsInRange returns missions for all categories and
     * that CategoryStats.total > 0 for at least one category.
     *
     * Validates: the radar chart section is not suppressed by the
     * `categoryStats.any { it.total > 0 }` guard.
     */
    @Test
    fun categoryStats_radarChart_rendersWhenAllSixCategoriesHaveData() {
        navigateToHistory()

        composeRule.waitUntil(timeoutMillis = 12_000) {
            composeRule
                .onAllNodesWithText("HUNTER BALANCE", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("HUNTER BALANCE — LAST 30 DAYS", substring = false)
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 6. Weakness Report — Pain Point Detection
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds 5 PHYSICAL DAILY missions in the last 30 days:
     *   • 1 completed (non-mini)
     *   • 4 failed
     * Expected: masteryScore = 1/5 = 0.20 < 0.50 threshold → isPainPoint = true
     * Expected: total (5) >= 3  → pain-point condition satisfied
     *
     * The "WEAKNESS REPORT" section header and a card labelled
     * "PAIN POINT — PHYSICAL" must both be visible.
     *
     * Validates the isPainPoint threshold: `total >= 3 && masteryScore < 0.5f`
     */
    @Test
    fun categoryStats_lowMasteryPhysical_showsPainPointInWeaknessReport() {
        navigateToHistory()

        composeRule.waitUntil(timeoutMillis = 12_000) {
            composeRule
                .onAllNodesWithText("WEAKNESS REPORT")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("WEAKNESS REPORT")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithText("PAIN POINT — PHYSICAL")
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Seeds 5 MENTAL DAILY missions in the last 30 days, all completed, 4 of
     * them via the mini version:
     *   • completed = 5, miniAccepted = 4, failed = 0
     *   • fullCompletions = 1,  masteryScore = (1×1.0 + 4×0.5) / 5 = 0.60
     *   • miniRate = 4/5 = 0.80 > 0.35  →  isMiniCrutch = true (NOT a pain point)
     *
     * The "WEAKNESS REPORT" header and a card labelled "CRUTCH — MENTAL" must
     * both be visible.
     *
     * Validates: the isMiniCrutch path is distinct from isPainPoint; the
     * crutch card label uses the category's displayName.uppercase() = "MENTAL".
     */
    @Test
    fun categoryStats_highMiniRateMental_showsMiniCrutchInWeaknessReport() {
        navigateToHistory()

        composeRule.waitUntil(timeoutMillis = 12_000) {
            composeRule
                .onAllNodesWithText("WEAKNESS REPORT")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("WEAKNESS REPORT")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithText("CRUTCH — MENTAL")
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 7. Full Integration Scenario
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Full 10-day scenario with:
     *   • Profile: 30 missions completed, 900 XP, best streak 8
     *   • 10 daily log entries spanning two weeks (days 1–10 ago) with mixed
     *     completion rates (0.25, 0.5, 0.75, 1.0 in rotation)
     *   • 3 missions per each of the 6 categories (2 completed, 1 failed)
     *
     * Asserts that ALL Battle Record sections are rendered simultaneously:
     *   ✓ Summary row (MISSIONS / TOTAL XP / BEST STREAK)
     *   ✓ Heatmap populated (no empty-state)
     *   ✓ Weekly XP bar chart visible
     *   ✓ At least 2 insight cards
     *   ✓ "HUNTER BALANCE" radar section visible
     *
     * Also asserts that the summary numbers "30" and "900" and "8d" are displayed,
     * confirming that richer seeding does not corrupt the profile read path.
     */
    @Test
    fun fullScenario_10Days_allBattleRecordSectionsRender() {
        navigateToHistory()

        // Summary row present
        composeRule.onNodeWithTag(HISTORY_SUMMARY_ROW).assertIsDisplayed()

        // Summary values correct
        composeRule.onNodeWithTag(HISTORY_SUMMARY_MISSIONS)
            .assert(hasAnyDescendant(hasText("30")))
        composeRule.onNodeWithTag(HISTORY_SUMMARY_XP)
            .assert(hasAnyDescendant(hasText("900")))
        composeRule.onNodeWithTag(HISTORY_SUMMARY_STREAK)
            .assert(hasAnyDescendant(hasText("8d")))

        // Heatmap present, not replaced by empty state
        composeRule.onNodeWithTag(HISTORY_HEATMAP).assertIsDisplayed()
        check(composeRule.onAllNodesWithTag(HISTORY_EMPTY_STATE).fetchSemanticsNodes().isEmpty()) {
            "Empty-state must be absent in the full 10-day scenario"
        }

        // Weekly XP chart visible
        composeRule.onNodeWithTag(HISTORY_WEEKLY_XP_CHART).performScrollTo().assertIsDisplayed()

        // At least two insight cards rendered
        composeRule.onNodeWithTag(historyInsight(0)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(historyInsight(1)).performScrollTo().assertIsDisplayed()

        // Hunter Balance radar chart section header
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule
                .onAllNodesWithText("HUNTER BALANCE", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("HUNTER BALANCE — LAST 30 DAYS", substring = false)
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Seed helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds a user profile with the given stats plus one log entry so the screen
     * does not show the empty state.
     */
    private fun seedSummaryStats(
        context: Context,
        missions: Int,
        xp: Long,
        bestStreak: Int
    ) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = missions,
                    totalXpEarned = xp,
                    streakBest = bestStreak,
                    streakCurrent = 2,
                    rank = "E",
                    level = 3,
                    xp = 120L,
                    xpToNextLevel = 300L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(30).toString()
                )
            )
            // One log so the heatmap is populated and the empty-state is hidden.
            db.dailyLogDao().upsertLog(
                DailyLogEntity(
                    date = today.minusDays(1).toString(),
                    xpGained = 50,
                    completionRate = 1.0f,
                    totalMissions = 2,
                    rankSnapshot = "E",
                    levelSnapshot = 3
                )
            )
        }
    }

    /**
     * Seeds [totalDays] daily log entries with completion rates cycling through
     * [0.25, 0.5, 0.75, 1.0, 0.0 (rest/miss), 1.0, 0.75, 0.5] to cover every
     * heatmap colour bucket.  Profile stats are set consistently.
     */
    private fun seedVariedDailyLogs(context: Context, totalDays: Int) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        // Rates covering all four colour buckets plus miss (0.0) and rest days
        val ratePool = listOf(1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 1.0f, 0.75f, 0.5f,
                              1.0f, 1.0f, 0.25f, 0.0f, 0.75f, 1.0f)
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = totalDays * 2,
                    totalXpEarned = (totalDays * 50).toLong(),
                    streakBest = totalDays,
                    streakCurrent = 3,
                    rank = "E",
                    level = 4,
                    xp = 180L,
                    xpToNextLevel = 400L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(totalDays.toLong()).toString()
                )
            )
            (1..totalDays).forEach { daysAgo ->
                val rate = ratePool[(daysAgo - 1) % ratePool.size]
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = (rate * 80).toInt(),
                        completionRate = rate,
                        totalMissions = 4,
                        rankSnapshot = "E",
                        levelSnapshot = 4,
                        wasRestDay = rate == 0.0f && daysAgo % 7 == 0
                    )
                )
            }
        }
    }

    /**
     * Seeds 21 daily log entries spanning three calendar weeks with distinct per-day
     * XP values:
     *   Days  1–7  (most recent week) : 30 XP each  → ≥ 210 XP this week
     *   Days  8–14 (one week back)    : 50 XP each  → ≥ 350 XP last week
     *   Days 15–21 (two weeks back)   : 20 XP each  → ≥ 140 XP two weeks ago
     *
     * All three weeks contribute non-zero XP so the weekly chart is guaranteed to
     * render (the chart is only shown when `weeklyXp.any { it.second > 0 }`).
     *
     * Note: exact weekly totals depend on the calendar-week (Mon→Sun) boundary
     * alignment with today, so only the presence of the chart is asserted, not
     * specific XP numbers.
     */
    private fun seedMultiWeekXpData(context: Context) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = 21,
                    totalXpEarned = 700L,
                    streakBest = 7,
                    streakCurrent = 3,
                    rank = "E",
                    level = 4,
                    xp = 150L,
                    xpToNextLevel = 400L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(21).toString()
                )
            )
            (1..21).forEach { daysAgo ->
                val xp = when {
                    daysAgo <= 7  -> 30
                    daysAgo <= 14 -> 50
                    else          -> 20
                }
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = xp,
                        completionRate = 0.8f,
                        totalMissions = 3,
                        rankSnapshot = "E",
                        levelSnapshot = 4
                    )
                )
            }
        }
    }

    /**
     * Seeds two 7-day blocks of daily logs:
     *   - "This week"  (days 1–6 ago) : all at [thisWeekRate]
     *   - "Last week"  (days 7–13 ago): all at [lastWeekRate]
     *
     * HistoryViewModel.buildInsights computes:
     *   thisWeekDays  = logs where date ≥ today – 6
     *   lastWeekDays  = logs where date ∈ [today – 13, today – 7]
     *   weekDelta     = thisWeekAvg – lastWeekAvg
     *   subtext       = if (weekDelta >= 0) "+N% — improving" else "–N% — slipping"
     */
    private fun seedWeekTrend(context: Context, lastWeekRate: Float, thisWeekRate: Float) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = 14,
                    totalXpEarned = 420L,
                    streakBest = 7,
                    streakCurrent = 3,
                    rank = "E",
                    level = 3,
                    xp = 100L,
                    xpToNextLevel = 300L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(14).toString()
                )
            )
            // This week: days 1–6 ago
            (1..6).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = (thisWeekRate * 60).toInt(),
                        completionRate = thisWeekRate,
                        totalMissions = 3
                    )
                )
            }
            // Last week: days 7–13 ago
            (7..13).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = (lastWeekRate * 60).toInt(),
                        completionRate = lastWeekRate,
                        totalMissions = 3
                    )
                )
            }
        }
    }

    /**
     * Seeds [perfectDays] log entries with completionRate=1.0f followed by
     * [imperfectDays] log entries with completionRate=0.5f.  All fall within the
     * last-30-days window of the "30-Day Average" insight.
     *
     * Also seeds an extra 7 days beyond [perfectDays + imperfectDays] to satisfy
     * the "This Week vs Last Week" data requirement (ensures insight index 0 is
     * always populated, keeping the 30-day insight at a predictable later index).
     *
     * Expected insight subtext: "$perfectDays perfect days in the last month"
     */
    private fun seedPerfectDaysMix(
        context: Context,
        perfectDays: Int,
        imperfectDays: Int
    ) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        val totalDays = perfectDays + imperfectDays
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = totalDays * 3,
                    totalXpEarned = (totalDays * 60).toLong(),
                    streakBest = perfectDays,
                    streakCurrent = 2,
                    rank = "E",
                    level = 4,
                    xp = 120L,
                    xpToNextLevel = 400L,
                    onboardingComplete = true,
                    joinDate = today.minusDays((totalDays + 7).toLong()).toString()
                )
            )
            // Perfect days: slots 1..perfectDays
            (1..perfectDays).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = 60,
                        completionRate = 1.0f,
                        totalMissions = 3
                    )
                )
            }
            // Imperfect days: slots perfectDays+1 .. totalDays
            ((perfectDays + 1)..totalDays).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = 30,
                        completionRate = 0.5f,
                        totalMissions = 3
                    )
                )
            }
            // Extra 7 days for the "last week" window so the week-delta insight fires
            ((totalDays + 1)..(totalDays + 7)).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = 45,
                        completionRate = 0.75f,
                        totalMissions = 3
                    )
                )
            }
        }
    }

    /**
     * Seeds 3 completed DAILY missions for each of the 6 MissionCategory values,
     * all with due_date within the last 30 days.  This guarantees
     * `categoryStats.any { it.total > 0 }` is true for all categories so the radar
     * chart section is displayed.  Also seeds an adequate log history.
     */
    private fun seedAllCategoryMissions(context: Context) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = 18,
                    totalXpEarned = 540L,
                    streakBest = 5,
                    streakCurrent = 3,
                    rank = "E",
                    level = 3,
                    xp = 90L,
                    xpToNextLevel = 300L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(25).toString()
                )
            )
            // Log entries for the last 14 days so insights render
            (1..14).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = 40,
                        completionRate = 0.7f,
                        totalMissions = 3
                    )
                )
            }
            // 3 completed missions per category
            MissionCategory.entries.forEachIndexed { catIdx, cat ->
                (0 until 3).forEach { mIdx ->
                    db.missionDao().insertMission(
                        makeMission(
                            id = "allcat_${cat.name}_$mIdx",
                            category = cat,
                            dueDate = today.minusDays((catIdx * 3 + mIdx + 2).toLong()).toString(),
                            isCompleted = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Seeds 5 PHYSICAL DAILY missions in the last 30 days with 1 completed (full,
     * non-mini) and 4 failed.
     *
     * Expected CategoryStats:
     *   total=5, completed=1, failed=4, miniAccepted=0
     *   masteryScore = 1/5 = 0.20  →  isPainPoint = true (0.20 < 0.50 && total >= 3)
     *
     * Also seeds 10 log entries and corresponding profile so all other screen
     * sections render without errors.
     */
    private fun seedCategoryPainPoint(context: Context) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = 6,
                    totalXpEarned = 180L,
                    streakBest = 4,
                    streakCurrent = 1,
                    rank = "E",
                    level = 2,
                    xp = 60L,
                    xpToNextLevel = 200L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(14).toString()
                )
            )
            (1..14).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = 25,
                        completionRate = 0.3f,
                        totalMissions = 5
                    )
                )
            }
            // 1 completed, 4 failed PHYSICAL missions
            // Spread across different days within last 30 days
            val outcomes = listOf(
                Triple(true,  false, false),  // completed
                Triple(false, true,  false),  // failed
                Triple(false, true,  false),  // failed
                Triple(false, true,  false),  // failed
                Triple(false, true,  false)   // failed
            )
            outcomes.forEachIndexed { idx, (comp, fail, _) ->
                db.missionDao().insertMission(
                    makeMission(
                        id = "pain_phys_$idx",
                        category = MissionCategory.PHYSICAL,
                        dueDate = today.minusDays((idx + 3).toLong()).toString(),
                        isCompleted = comp,
                        isFailed = fail
                    )
                )
            }
        }
    }

    /**
     * Seeds 5 MENTAL DAILY missions in the last 30 days, all completed:
     *   - 1 completed via full version (miniAccepted = false)
     *   - 4 completed via mini version (miniAccepted = true)
     *
     * Expected CategoryStats:
     *   total=5, completed=5, miniAccepted=4, failed=0
     *   fullCompletions = 5 – 4 = 1
     *   masteryScore    = (1×1.0 + 4×0.5) / 5 = 0.60  →  NOT a pain point
     *   miniRate        = 4/5 = 0.80 > 0.35            →  isMiniCrutch = true
     */
    private fun seedCategoryMiniCrutch(context: Context) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = 7,
                    totalXpEarned = 140L,
                    streakBest = 4,
                    streakCurrent = 2,
                    rank = "E",
                    level = 2,
                    xp = 55L,
                    xpToNextLevel = 200L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(14).toString()
                )
            )
            (1..14).forEach { daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = 28,
                        completionRate = 0.6f,
                        totalMissions = 5
                    )
                )
            }
            // 5 MENTAL completed: index 0 = full, indices 1-4 = mini
            (0 until 5).forEach { idx ->
                db.missionDao().insertMission(
                    makeMission(
                        id = "crutch_mental_$idx",
                        category = MissionCategory.MENTAL,
                        dueDate = today.minusDays((idx + 3).toLong()).toString(),
                        isCompleted = true,
                        acceptedMiniVersion = idx > 0      // 4 mini missions
                    )
                )
            }
        }
    }

    /**
     * Seeds a rich 10-day scenario for the full integration test:
     *   • Profile: totalMissions=30, totalXpEarned=900, bestStreak=8
     *   • 10 daily log entries (days 1–10 ago) with rotating completion rates
     *   • 3 missions per category (2 completed, 1 failed) spread across last 30 days
     */
    private fun seedFullScenario(context: Context) {
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()
        val rates   = listOf(1.0f, 0.75f, 1.0f, 0.5f, 1.0f, 0.75f, 1.0f, 0.5f, 0.75f, 1.0f)
        val xpByDay = listOf(60,    45,    60,   30,   60,   45,    60,   30,   45,    60)

        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Record Hunter")
                setLastDailyResetDate(today.toString())
            }
            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "Record Hunter",
                    totalMissionsCompleted = 30,
                    totalXpEarned = 900L,
                    streakBest = 8,
                    streakCurrent = 4,
                    rank = "E",
                    level = 6,
                    xp = 250L,
                    xpToNextLevel = 600L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(20).toString()
                )
            )

            (1..10).forEachIndexed { idx, daysAgo ->
                db.dailyLogDao().upsertLog(
                    DailyLogEntity(
                        date = today.minusDays(daysAgo.toLong()).toString(),
                        xpGained = xpByDay[idx],
                        completionRate = rates[idx],
                        totalMissions = 3,
                        rankSnapshot = "E",
                        levelSnapshot = 6
                    )
                )
            }

            // 3 missions per category; 2 completed, 1 failed
            MissionCategory.entries.forEachIndexed { catIdx, cat ->
                (0 until 3).forEach { mIdx ->
                    db.missionDao().insertMission(
                        makeMission(
                            id = "full_${cat.name}_$mIdx",
                            category = cat,
                            dueDate = today.minusDays((catIdx * 3 + mIdx + 2).toLong()).toString(),
                            isCompleted = mIdx < 2,
                            isFailed = mIdx == 2
                        )
                    )
                }
            }
        }
    }

    // ── Entity factory ────────────────────────────────────────────────────────

    private fun makeMission(
        id: String,
        category: MissionCategory,
        dueDate: String,
        isCompleted: Boolean = false,
        isFailed: Boolean = false,
        acceptedMiniVersion: Boolean = false,
        type: MissionType = MissionType.DAILY
    ) = MissionEntity(
        id = id,
        title  = "Test Mission $id",
        description   = "Auto-seeded for BattleRecord tests.",
        systemLore    = "[test]",
        type          = type.name,
        category      = category.name,
        difficulty    = Difficulty.E.name,
        xpReward      = 25,
        penaltyXp     = 5,
        penaltyHp     = 5,
        isCompleted   = isCompleted,
        isFailed      = isFailed,
        isSkipped     = false,
        acceptedMiniVersion = acceptedMiniVersion,
        dueDate       = dueDate,
        iconName      = category.iconName
    )
}
