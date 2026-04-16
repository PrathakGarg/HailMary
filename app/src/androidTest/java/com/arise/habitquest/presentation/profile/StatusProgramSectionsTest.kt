package com.arise.habitquest.presentation.profile

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.ProgressionPreference
import com.arise.habitquest.domain.model.ScheduleStyle
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.presentation.progression.ProgramDirective
import com.arise.habitquest.presentation.progression.ProgramDirectivesSection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatusProgramSectionsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun programProfileSection_rendersTrackPacingScheduleAndRisks() {
        composeRule.setContent {
            StatusTestTheme {
                ProgramProfileSection(
                    profile = UserProfile(
                        hunterName = "tester",
                        trackFocus = MissionCategory.PRODUCTIVITY,
                        progressionPreference = ProgressionPreference.AGGRESSIVE,
                        scheduleStyle = ScheduleStyle.FLEXIBLE_SPLIT,
                        shoulderRiskFlag = true,
                        heatRiskFlag = false
                    )
                )
            }
        }

        composeRule.onNodeWithTag("status_track_focus").assertIsDisplayed()
        composeRule.onNodeWithText("Productivity").assertIsDisplayed()
        composeRule.onNodeWithTag("status_pacing").assertIsDisplayed()
        composeRule.onNodeWithText("Aggressive").assertIsDisplayed()
        composeRule.onNodeWithTag("status_schedule_style").assertIsDisplayed()
        composeRule.onNodeWithText("Flexible Split").assertIsDisplayed()
        composeRule.onNodeWithTag("status_risk_flags").assertIsDisplayed()
        composeRule.onNodeWithText("Shoulder").assertIsDisplayed()
    }

    @Test
    fun systemDirectivesSection_rendersSharedDirectiveCardsForStatus() {
        composeRule.setContent {
            StatusTestTheme {
                ProgramDirectivesSection(
                    directives = listOf(
                        ProgramDirective(
                            label = "PROGRESSION STATE",
                            value = "Re ramp",
                            detail = "Recent completion is 86%. Recovery signals are strong enough to start scaling back up.",
                            isWarning = false
                        )
                    ),
                    tagPrefix = "status_program_directive"
                )
            }
        }

        composeRule.onNodeWithText("PROGRESSION STATE").assertIsDisplayed()
        composeRule.onNodeWithText("Re ramp").assertIsDisplayed()
    }
}

@Composable
private fun StatusTestTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}