package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_COMPLETE
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_FAIL
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.missionCard
import com.arise.habitquest.e2e.support.E2ESelectors.missionQuickComplete
import com.arise.habitquest.e2e.support.E2ESelectors.missionsListPage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeMissionBoardSyncE2ETest {

    private val missionId = "e2e_daily_sync_1"

    private val resetRule = ResetAppStateRule()

    private val seedMissionRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2ETestHarness.seedReturningUserWithActiveDailyMission(
                missionId = missionId,
                missionTitle = "E2E Sync Mission",
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
    fun quickCompleteOnHome_syncsToMissionBoard() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        E2EAssertions.waitForTag(composeRule, missionCard(missionId))
        E2EAssertions.waitForDisplayedTag(composeRule, missionQuickComplete(missionId))

        composeRule.onNodeWithTag(missionQuickComplete(missionId)).performClick()

        composeRule.waitUntil(timeoutMillis = 12_000) {
            E2ETestHarness.isMissionCompleted(missionId)
        }

        composeRule.onNodeWithTag(BOTTOM_NAV_MISSIONS).performClick()

        E2EAssertions.waitForTag(composeRule, missionsListPage(0))
        E2EAssertions.waitForTag(composeRule, missionCard(missionId))
        composeRule.onNodeWithTag(missionCard(missionId)).performClick()

        E2EAssertions.waitForTag(composeRule, MISSION_DETAIL_SCREEN)
        check(tagAbsent(MISSION_DETAIL_COMPLETE))
        check(tagAbsent(MISSION_DETAIL_FAIL))
    }

    private fun tagAbsent(tag: String): Boolean {
        return composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
    }
}