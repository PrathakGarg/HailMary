package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_COMPLETE_RETURN
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_COMPLETE_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_COMPLETE
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_FAIL
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.missionCard
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissionDetailFlowE2ETest {

    private val missionId = "e2e_daily_1"

    private val resetRule = ResetAppStateRule()

    private val seedMissionRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2ETestHarness.seedReturningUserWithActiveDailyMission(
                missionId = missionId,
                missionTitle = "E2E Mission ${description.methodName}",
                context = context
            )
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
        .around(seedMissionRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun missionDetail_completeRoutesToCompletionAndReturnsHome() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        E2EAssertions.waitForTag(composeRule, missionCard(missionId))

        composeRule.onNodeWithTag(missionCard(missionId)).performClick()

        E2EAssertions.waitForTag(composeRule, MISSION_DETAIL_SCREEN)
        composeRule.onNodeWithTag(MISSION_DETAIL_COMPLETE).performClick()

        E2EAssertions.waitForTag(composeRule, MISSION_COMPLETE_SCREEN)
        E2EAssertions.waitForDisplayedTag(composeRule, MISSION_COMPLETE_RETURN, timeoutMillis = 20_000)
        composeRule.onNodeWithTag(MISSION_COMPLETE_RETURN).performClick()

        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        waitForMissionCompletionState { E2ETestHarness.isMissionCompleted(missionId) }
    }

    @Test
    fun missionDetail_failReturnsHomeWithFailedState() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        E2EAssertions.waitForTag(composeRule, missionCard(missionId))

        composeRule.onNodeWithTag(missionCard(missionId)).performClick()

        E2EAssertions.waitForTag(composeRule, MISSION_DETAIL_SCREEN)
        composeRule.onNodeWithTag(MISSION_DETAIL_FAIL).performClick()

        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        waitForMissionCompletionState { E2ETestHarness.isMissionFailed(missionId) }
    }

    private fun waitForMissionCompletionState(predicate: () -> Boolean) {
        composeRule.waitUntil(timeoutMillis = 12_000) {
            predicate()
        }
    }
}