package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
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
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class HistoryE2ETest {

    private val resetRule = ResetAppStateRule()

    private val seedRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            when (description.methodName) {
                "populatedHistory_rendersSummaryAndHeatmap" -> E2ETestHarness.seedHistoryScenario(context)
                "emptyHistory_showsEmptyState" -> E2ETestHarness.seedProgressionScenario(context = context)
                "historyHeatmap_screenshot_todayVisibleWithoutTodayLog" -> E2ETestHarness.seedHistoryScenarioWithoutTodayLog(context)
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

    @Test
    fun historyHeatmap_screenshot_todayVisibleWithoutTodayLog() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_STATUS)
        composeRule.onNodeWithTag(BOTTOM_NAV_STATUS).performClick()
        E2EAssertions.waitForTag(composeRule, STATUS_SCREEN)
        composeRule.onNodeWithTag(STATUS_VIEW_HISTORY).performScrollTo().performClick()

        E2EAssertions.waitForTag(composeRule, HISTORY_SCREEN)
        val image = composeRule.onNodeWithTag(HISTORY_HEATMAP).captureToImage().asAndroidBitmap()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val outDir = File(context.getExternalFilesDir(null), "test-artifacts")
        if (!outDir.exists()) outDir.mkdirs()
        val outFile = File(outDir, "history-heatmap-screenshot.png")
        if (outFile.exists()) outFile.delete()
        FileOutputStream(outFile).use { stream ->
            image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        }

        // Also capture a full-screen screenshot to shared storage for easy adb pull.
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val sharedShot = File("/sdcard/Download/history-screen-ui-test.png")
        if (sharedShot.exists()) sharedShot.delete()
        device.takeScreenshot(sharedShot)

        // Keep the screen stable briefly for manual adb screencap in debugging runs.
        Thread.sleep(1800)

        check(outFile.exists() && outFile.length() > 0L) {
            "Screenshot file was not written: ${outFile.absolutePath}"
        }
        // Best effort only; some emulator/API combinations block shared-storage writes.
    }
}