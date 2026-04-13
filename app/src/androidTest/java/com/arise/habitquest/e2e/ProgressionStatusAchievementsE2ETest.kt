package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.ACHIEVEMENT_DETAIL_DIALOG
import com.arise.habitquest.e2e.support.E2ESelectors.ACHIEVEMENT_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.ACHIEVEMENT_SUMMARY
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_STATUS
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_COMPLETE_RETURN
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_COMPLETE_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_COMPLETE
import com.arise.habitquest.e2e.support.E2ESelectors.MISSION_DETAIL_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_BEST_STREAK
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_CURRENT_STREAK
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_RANK_LEVEL
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_TOTAL_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_TOTAL_XP_EARNED
import com.arise.habitquest.e2e.support.E2ESelectors.STATUS_VIEW_ACHIEVEMENTS
import com.arise.habitquest.e2e.support.E2ESelectors.achievementCard
import com.arise.habitquest.e2e.support.E2ESelectors.missionCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressionStatusAchievementsE2ETest {

    private val missionId = "e2e_progress_daily"

    private val resetRule = ResetAppStateRule()

    private val seedProgressionRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2ETestHarness.seedProgressionScenario(
                missionId = missionId,
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
        .around(seedProgressionRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun completingThresholdMission_updatesProgressionStatusAndAchievements() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        E2EAssertions.waitForTag(composeRule, missionCard(missionId))

        composeRule.onNodeWithTag(missionCard(missionId)).performClick()
        E2EAssertions.waitForTag(composeRule, MISSION_DETAIL_SCREEN)
        composeRule.onNodeWithTag(MISSION_DETAIL_COMPLETE).performClick()

        E2EAssertions.waitForTag(composeRule, MISSION_COMPLETE_SCREEN)
        E2EAssertions.waitForDisplayedTag(composeRule, MISSION_COMPLETE_RETURN, timeoutMillis = 20_000)
        composeRule.onNodeWithTag(MISSION_COMPLETE_RETURN).performClick()

        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.waitUntil(timeoutMillis = 12_000) {
            E2ETestHarness.getUserProfileEntity()?.let {
                it.level == 2 &&
                    it.xp == 135L &&
                    it.totalMissionsCompleted == 1 &&
                    it.totalXpEarned == 20L &&
                    it.streakCurrent == 3 &&
                    it.streakBest == 3
            } == true && E2ETestHarness.getUnlockedAchievementCount() == 2
        }

        val profile = requireNotNull(E2ETestHarness.getUserProfileEntity())
        assertEquals(2, profile.level)
        assertEquals(135L, profile.xp)
        assertEquals(1, profile.totalMissionsCompleted)
        assertEquals(20L, profile.totalXpEarned)
        assertEquals(3, profile.streakCurrent)
        assertEquals(3, profile.streakBest)
        assertEquals(2, E2ETestHarness.getUnlockedAchievementCount())
        assertTrue(E2ETestHarness.isAchievementUnlocked("ach_first_gate"))
        assertTrue(E2ETestHarness.isAchievementUnlocked("ach_streak_3"))
        assertFalse(E2ETestHarness.isAchievementUnlocked("ach_level_10"))

        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).performClick()
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)

        composeRule.onNodeWithText("LEVEL 2", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(STATUS_TOTAL_MISSIONS).assertTextContains("1")
        composeRule.onNodeWithTag(STATUS_TOTAL_XP_EARNED).assertTextContains("20")
        composeRule.onNodeWithTag(STATUS_CURRENT_STREAK).assertTextContains("3 days")
        composeRule.onNodeWithTag(STATUS_BEST_STREAK).assertTextContains("3 days")

        composeRule.onNodeWithTag(STATUS_VIEW_ACHIEVEMENTS).performScrollTo().performClick()
        E2EAssertions.waitForTag(composeRule, ACHIEVEMENT_SCREEN)
        composeRule.onNodeWithTag(ACHIEVEMENT_SUMMARY).assertTextContains("2/3")
        composeRule.onNodeWithTag(achievementCard("ach_first_gate")).performClick()
        E2EAssertions.waitForTag(composeRule, ACHIEVEMENT_DETAIL_DIALOG)
        composeRule.onNodeWithText("CLOSE").assertIsDisplayed()
    }
}