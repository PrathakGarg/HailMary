package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissionBoardNavigationE2ETest {

    @get:Rule
    val loggingRule = E2ETestLoggingRule()

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @get:Rule
    val grantNotificationPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @Test
    fun homeToMissions_tabsAndSwipeWork() {
        E2ETestHarness.resetToFreshInstallState()
        E2ETestHarness.setOnboardingComplete(true)

        ActivityScenario.launch(MainActivity::class.java)

        waitForTag("bottom_nav_missions")
        composeRule.onNodeWithTag("bottom_nav_missions").performClick()

        waitForTag("missions_tab_row")
        composeRule.onNodeWithTag("missions_tab_row").assertIsDisplayed()
        composeRule.onNodeWithTag("missions_tab_0").assertIsDisplayed()
        composeRule.onNodeWithTag("missions_tab_1").assertIsDisplayed()

        // Swipe once to move from DAILY (index 0) to WEEKLY (index 1)
        composeRule.onNodeWithTag("missions_pager").performTouchInput { swipeLeft() }

        composeRule.waitUntil(timeoutMillis = 12_000) {
            hasTag("missions_list_page_1") || hasTag("missions_empty_page_1")
        }
        if (hasTag("missions_list_page_1")) {
            composeRule.onNodeWithTag("missions_list_page_1").assertIsDisplayed()
        } else {
            composeRule.onNodeWithTag("missions_empty_page_1").assertIsDisplayed()
        }
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 12_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun hasTag(tag: String): Boolean {
        return composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
}
