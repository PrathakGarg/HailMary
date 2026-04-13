package com.arise.habitquest.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arise.habitquest.presentation.onboarding.OnboardingNavBar
import com.arise.habitquest.ui.theme.AriseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingNavBarSmokeE2ETest {

    @get:Rule
    val loggingRule = E2ETestLoggingRule()

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun phaseOne_showsBackAndNext_andBackInvokesCallback() {
        var backCount = 0
        var nextCount = 0

        composeRule.setContent {
            AriseTheme {
                OnboardingNavBar(
                    phase = 1,
                    canProceed = true,
                    isLoading = false,
                    onBack = { backCount++ },
                    onNext = { nextCount++ }
                )
            }
        }

        composeRule.onNodeWithTag("onboarding_back").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_next").assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.onNodeWithTag("onboarding_next").performClick()

        assertEquals(1, backCount)
        assertEquals(1, nextCount)
    }
}
