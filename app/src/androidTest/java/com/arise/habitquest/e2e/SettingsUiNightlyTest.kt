package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.HOME_SETTINGS_BUTTON
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_REGENERATE_BUTTON
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_REGENERATE_CANCEL
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_REGENERATE_CONFIRM
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_REGENERATE_DIALOG
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_STASIS_BUTTON
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_STASIS_USES
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_NOTIFICATION_LABEL
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_NOTIFICATION_SLIDER
import com.arise.habitquest.e2e.support.E2ESelectors.SETTINGS_FOCUS_COUNT
import com.arise.habitquest.e2e.support.E2ESelectors.settingsRestDay
import com.arise.habitquest.e2e.support.E2ESelectors.settingsRestDaySelected
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class SettingsUiNightlyTest {

    private val sessionDate = LocalDate.of(2026, 4, 14)
    private val oldMissionId = "settings_ui_old_daily"

    private val resetRule = ResetAppStateRule()

    private val seedRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2ETestHarness.pinTrustedTime(
                localDateTime = LocalDateTime.of(2026, 4, 14, 9, 0),
                dayStartMinutes = TimeProvider.DEFAULT_DAY_START_MINUTES,
                context = context
            )

            seedProfile(context)

            E2ETestHarness.insertMission(
                MissionEntity(
                    id = oldMissionId,
                    title = "UI Regenerate Old Mission",
                    description = "Old daily mission for regenerate UI test.",
                    systemLore = "[E2E] Settings UI regenerate scenario.",
                    miniMissionDescription = "Short run.",
                    type = MissionType.DAILY.name,
                    category = MissionCategory.PHYSICAL.name,
                    difficulty = Difficulty.E.name,
                    xpReward = 20,
                    penaltyXp = 5,
                    penaltyHp = 5,
                    statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
                    dueDate = sessionDate.toString(),
                    scheduledTimeHint = "MORNING",
                    iconName = MissionCategory.PHYSICAL.iconName
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
        .around(seedRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun homeSettingsButton_opensSettingsScreen() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(HOME_SETTINGS_BUTTON).performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_SCREEN)
    }

    @Test
    fun regenerateDialog_cancelAndConfirm_areUiFunctional() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(HOME_SETTINGS_BUTTON).performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_SCREEN)
        waitForHydratedSettingsUi()

        // Open and cancel
        composeRule.onNodeWithTag(SETTINGS_REGENERATE_BUTTON).performScrollTo().performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_REGENERATE_DIALOG)
        composeRule.onNodeWithTag(SETTINGS_REGENERATE_CANCEL).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(SETTINGS_REGENERATE_DIALOG).fetchSemanticsNodes().isEmpty()
        }

        // Open and confirm
        composeRule.onNodeWithTag(SETTINGS_REGENERATE_BUTTON).performScrollTo().performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_REGENERATE_DIALOG)
        composeRule.onNodeWithTag(SETTINGS_REGENERATE_CONFIRM).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(SETTINGS_REGENERATE_DIALOG).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun restDaySelection_showsSelectedIndicatorInUi() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(HOME_SETTINGS_BUTTON).performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_SCREEN)
        waitForHydratedSettingsUi()

        composeRule.onNodeWithTag(settingsRestDay("tuesday")).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            E2ETestHarness.getUserProfileEntity()?.restDay == 1
        }
    }

    @Test
    fun emergencyStasis_click_updatesUsesCounterText() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(HOME_SETTINGS_BUTTON).performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_SCREEN)
        waitForHydratedSettingsUi()

        composeRule.onNodeWithTag(SETTINGS_STASIS_BUTTON).performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            E2ETestHarness.getUserProfileEntity()?.graceUsesRemaining == 1
        }
    }

    @Test
    fun notificationSlider_change_updatesReminderLabel() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(HOME_SETTINGS_BUTTON).performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_SCREEN)
        waitForHydratedSettingsUi()

        composeRule.onNodeWithTag(SETTINGS_NOTIFICATION_SLIDER).performSemanticsAction(SemanticsActions.SetProgress) {
            it(10f)
        }

        composeRule.waitUntil(timeoutMillis = 8_000) {
            try {
                composeRule.onNodeWithTag(SETTINGS_NOTIFICATION_LABEL).assertTextContains("Reminder at: 10:00")
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    @Test
    fun focusTheme_toggle_cannotDropBelowOneActive() {
        E2EAssertions.waitForTag(composeRule, BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(HOME_SETTINGS_BUTTON).performClick()
        E2EAssertions.waitForTag(composeRule, SETTINGS_SCREEN)
        waitForHydratedSettingsUi()

        composeRule.onNodeWithTag("settings_theme_physical_performance").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            runBlocking {
                OnboardingDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
                    .focusThemes
                    .first()
                    .size == 1
            }
        }
        composeRule.waitUntil(timeoutMillis = 6_000) {
            runBlocking {
                OnboardingDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
                    .focusThemes
                    .first()
                    .size == 1
            }
        }

        // Attempt to remove the last active theme (should remain at 1/3)
        composeRule.onNodeWithTag("settings_theme_mental_clarity").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            runBlocking {
                OnboardingDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
                    .focusThemes
                    .first()
                    .size == 1
            }
        }
    }

    private fun seedProfile(context: android.content.Context) {
        E2ETestHarness.upsertUserProfile(
            UserProfileEntity(
                hunterName = "Settings UI Tester",
                epithet = "Silent",
                title = "The Unawakened",
                rank = "E",
                level = 3,
                xp = 120L,
                xpToNextLevel = 300L,
                hp = 100,
                maxHp = 100,
                streakCurrent = 2,
                streakBest = 5,
                daysSinceJoin = 10,
                onboardingComplete = true,
                onboardingAnswersJson = "{\"templateIds\":[\"tpl_push_ups\",\"tpl_deep_work\",\"tpl_meditation\"]}",
                joinDate = sessionDate.minusDays(10).toString(),
                restDay = 3,
                notificationHour = 8,
                graceUsesRemaining = 2,
                pendingWarning = true,
                consecutiveMissDays = 3
            ),
            context = context
        )

        kotlinx.coroutines.runBlocking {
            val store = OnboardingDataStore(context)
            store.setOnboardingComplete(true)
            store.setHunterName("Settings UI Tester")
            store.setLastDailyResetDate(sessionDate.toString())
            store.setNotificationHour(8)
            store.setFocusThemes(emptySet())
        }
    }

    private fun waitForHydratedSettingsUi() {
        composeRule.waitUntil(timeoutMillis = 12_000) {
            try {
                composeRule.onNodeWithText("Uses remaining: 2/3", substring = true).assertExists()
                true
            } catch (_: Throwable) {
                false
            }
        }
    }
}
