package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_STATUS
import com.arise.habitquest.e2e.support.E2ESelectors.HISTORY_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_VIEW_HISTORY
import com.arise.habitquest.e2e.support.E2ESelectors.missionsListPage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteRegressionE2ETest {

    private val resetRule = ResetAppStateRule()

    private val seedRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2ETestHarness.seedReturningUserWithActiveDailyMission(context = context)
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
    fun routeRegression_bottomNavHistoryAndBack_returnToExpectedSurface() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)

        composeRule.onNodeWithTag(BOTTOM_NAV_MISSIONS).performClick()
        E2EAssertions.waitForTag(composeRule, missionsListPage(0))

        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).performClick()
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)
        composeRule.onNodeWithTag(STATUS_VIEW_HISTORY).performScrollTo().performClick()
        E2EAssertions.waitForTag(composeRule, HISTORY_SCREEN)

        composeRule.runOnIdle {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)
        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).assertIsDisplayed()
    }
}
