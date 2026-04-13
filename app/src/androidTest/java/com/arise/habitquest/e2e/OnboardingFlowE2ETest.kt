package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_HOME
import com.arise.habitquest.e2e.support.E2ESelectors.BOTTOM_NAV_MISSIONS
import com.arise.habitquest.e2e.support.E2ESelectors.ONBOARDING_NEXT
import com.arise.habitquest.e2e.support.E2ESelectors.REGISTRATION_COMPLETE_ENTER
import com.arise.habitquest.e2e.support.E2ESelectors.REGISTRATION_COMPLETE_SCREEN
import com.arise.habitquest.e2e.support.E2ESelectors.onboardingPhase
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingFlowE2ETest {

    private val grantNotificationPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    private val resetRule = ResetAppStateRule()
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(resetRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun onboardingToHome_happyPath() {
        composeRule.waitUntil(timeoutMillis = 20_000) {
            hasTag(ONBOARDING_NEXT) || hasTag(BOTTOM_NAV_HOME) || hasTag(onboardingPhase(0))
        }

        // If app is already routed home, validate tabs and exit.
        if (hasTag(BOTTOM_NAV_HOME)) {
            composeRule.onNodeWithTag(BOTTOM_NAV_HOME).assertIsDisplayed()
            composeRule.onNodeWithTag(BOTTOM_NAV_MISSIONS).assertIsDisplayed()
            return
        }

        // Phase 0 requirements
        composeRule.onNode(hasSetTextAction()).performTextInput("Test Hunter")
        composeRule.onNodeWithText("Silent").performClick()
        composeRule.onNodeWithText("Fierce").performClick()
        composeRule.onNodeWithText("Relentless").performClick()
        composeRule.onNodeWithTag(ONBOARDING_NEXT).performClick()
        waitForTag(onboardingPhase(1))

        // Phase 1 requirement: at least one goal
        composeRule.onNodeWithText("Physical Fitness").performClick()
        composeRule.onNodeWithTag(ONBOARDING_NEXT).performClick()
        waitForTag(onboardingPhase(2))

        // Phases 2-4 can proceed with defaults
        composeRule.onNodeWithTag(ONBOARDING_NEXT).performClick()
        waitForTag(onboardingPhase(3))
        composeRule.onNodeWithTag(ONBOARDING_NEXT).performClick()
        waitForTag(onboardingPhase(4))
        composeRule.onNodeWithTag(ONBOARDING_NEXT).performClick()
        waitForTag(onboardingPhase(5))

        // Phase 5 completion -> registration screen
        composeRule.onNodeWithTag(ONBOARDING_NEXT).performClick()

        waitForTag(REGISTRATION_COMPLETE_SCREEN)
        waitForTag(REGISTRATION_COMPLETE_ENTER, timeoutMillis = 20_000)
        composeRule.onNodeWithTag(REGISTRATION_COMPLETE_ENTER).performClick()

        // Land on home tabs
        waitForTag(BOTTOM_NAV_HOME)
        composeRule.onNodeWithTag(BOTTOM_NAV_HOME).assertIsDisplayed()
        composeRule.onNodeWithTag(BOTTOM_NAV_MISSIONS).assertIsDisplayed()
    }

    private fun hasTag(tag: String): Boolean {
        return composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            runCatching {
                composeRule.onNodeWithTag(tag).assertIsDisplayed()
            }.isSuccess
        }
    }
}
