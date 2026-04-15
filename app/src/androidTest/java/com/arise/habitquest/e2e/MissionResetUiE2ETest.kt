package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.missionCard
import com.arise.habitquest.e2e.support.E2ESelectors.missionReset
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissionResetUiE2ETest {

    private val resetMissionId = "e2e_reset_done_1"

    private val resetRule = ResetAppStateRule()

    private val seedMissionRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2ETestHarness.seedReturningUserWithActiveDailyMission(context = context)
            val sessionDate = E2ETestHarness.getSessionDay(context)
            E2ETestHarness.insertMission(
                MissionEntity(
                    id = resetMissionId,
                    title = "Legacy Completed Mission",
                    description = "Seeded completed mission to test reset button stability.",
                    systemLore = "[E2E] Reset button stability check",
                    miniMissionDescription = "",
                    type = "DAILY",
                    category = "PRODUCTIVITY",
                    difficulty = "E",
                    xpReward = 15,
                    penaltyXp = 5,
                    penaltyHp = 5,
                    statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    dueDate = sessionDate.toString(),
                    scheduledTimeHint = "MORNING",
                    iconName = "task_alt"
                ),
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
    fun resetFromHome_completedMission_noCrash_andMissionReopens() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        E2EAssertions.waitForTag(composeRule, missionCard(resetMissionId))
        composeRule.onNodeWithTag(missionCard(resetMissionId)).performScrollTo()
        E2EAssertions.waitForTag(composeRule, missionReset(resetMissionId))
        composeRule.onNodeWithTag(missionReset(resetMissionId), useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithTag(missionReset(resetMissionId), useUnmergedTree = true).performTouchInput {
            click()
        }

        composeRule.waitUntil(timeoutMillis = 12_000) {
            !E2ETestHarness.isMissionCompleted(resetMissionId) && !E2ETestHarness.isMissionFailed(resetMissionId)
        }

        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
    }

    @Test
    fun resetFromMissionBoard_completedMission_noCrash_andMissionReopens() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(BOTTOM_NAV_MISSIONS).performClick()

        E2EAssertions.waitForTag(composeRule, missionCard(resetMissionId))
        composeRule.onNodeWithTag(missionCard(resetMissionId)).performScrollTo()
        E2EAssertions.waitForTag(composeRule, missionReset(resetMissionId))
        composeRule.onNodeWithTag(missionReset(resetMissionId), useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithTag(missionReset(resetMissionId), useUnmergedTree = true).performTouchInput {
            click()
        }

        composeRule.waitUntil(timeoutMillis = 12_000) {
            !E2ETestHarness.isMissionCompleted(resetMissionId) && !E2ETestHarness.isMissionFailed(resetMissionId)
        }

        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_MISSIONS)
    }
}
