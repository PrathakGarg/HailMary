package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_STATUS
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_EMPTY_STATE
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_HEATMAP
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_WEEKLY_XP_CHART
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_ROW
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_STREAK
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SUMMARY_XP
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_VIEW_HISTORY
import com.arise.habitquest.e2e.support.E2ESelectors.historyInsight
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryE2ETest {

    private val resetRule = ResetAppStateRule()

    private val seedRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            when (description.methodName) {
                "populatedHistory_rendersSummaryAndHeatmap" -> E2ETestHarness.seedHistoryScenario(context)
                "emptyHistory_showsEmptyState" -> E2ETestHarness.seedProgressionScenario(context = context)
            }
        }
    }

    private val grantNotificationPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(resetRule)
        .around(seedRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun populatedHistory_rendersSummaryAndHeatmap() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_STATUS)
        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).performClick()
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)
        composeRule.onNodeWithTag(STATUS_VIEW_HISTORY).performScrollTo().performClick()

        E2EAssertions.waitForTag(composeRule, HISTORY_SCREEN)
        composeRule.onNodeWithTag(HISTORY_SUMMARY_ROW).assertIsDisplayed()
        composeRule.onNodeWithTag(HISTORY_SUMMARY_MISSIONS).assertIsDisplayed()
        composeRule.onNodeWithTag(HISTORY_SUMMARY_XP).assertIsDisplayed()
        composeRule.onNodeWithTag(HISTORY_SUMMARY_STREAK).assertIsDisplayed()
        composeRule.onNodeWithTag(HISTORY_HEATMAP).assertIsDisplayed()
        composeRule.onNodeWithTag(HISTORY_WEEKLY_XP_CHART).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(historyInsight(0)).performScrollTo().assertIsDisplayed()
        check(composeRule.onAllNodesWithTag(HISTORY_EMPTY_STATE).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun emptyHistory_showsEmptyState() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_STATUS)
        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).performClick()
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)
        composeRule.onNodeWithTag(STATUS_VIEW_HISTORY).performScrollTo().performClick()

        E2EAssertions.waitForTag(composeRule, HISTORY_SCREEN)
        E2EAssertions.waitForTag(composeRule, HISTORY_EMPTY_STATE)
        composeRule.onNodeWithTag(HISTORY_EMPTY_STATE).performScrollTo().assertIsDisplayed()
    }
}