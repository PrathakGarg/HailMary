package com.arise.habitquest.presentation.history

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arise.habitquest.domain.model.MuscleRegion
import com.arise.habitquest.presentation.progression.ProgramDirective
import com.arise.habitquest.presentation.progression.ProgramDirectivesSection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryExplainabilityUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun programDirectivesSection_rendersDirectiveCards() {
        composeRule.setContent {
            TestTheme {
                ProgramDirectivesSection(
                    directives = listOf(
                        ProgramDirective(
                            label = "PROGRESSION STATE",
                            value = "Deload",
                            detail = "Recent completion is 32%. The system is temporarily reducing pressure to stabilize consistency.",
                            isWarning = true
                        ),
                        ProgramDirective(
                            label = "TRANSITION WATCH",
                            value = "Bias toward Wellness",
                            detail = "Recent struggle signals suggest shifting emphasis toward wellness until momentum returns.",
                            isWarning = false
                        )
                    )
                )
            }
        }

        composeRule.onNodeWithText("PROGRESSION STATE").assertIsDisplayed()
        composeRule.onNodeWithText("Deload").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Recent completion is 32%. The system is temporarily reducing pressure to stabilize consistency."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("TRANSITION WATCH").assertIsDisplayed()
        composeRule.onNodeWithText("Bias toward Wellness").assertIsDisplayed()
    }

    @Test
    fun weeklyBodyCoverageChart_rendersLegendAndCoveragePercentages() {
        composeRule.setContent {
            TestTheme {
                WeeklyBodyCoverageChart(
                    coverage = listOf(
                        MuscleCoverageEntry(
                            region = MuscleRegion.CORE,
                            assignedLoad = 1f,
                            completedLoad = 0.3f,
                            completionRatio = 0.3f
                        ),
                        MuscleCoverageEntry(
                            region = MuscleRegion.QUADS,
                            assignedLoad = 0.8f,
                            completedLoad = 0.6f,
                            completionRatio = 0.75f
                        )
                    )
                )
            }
        }

        composeRule.onNodeWithText("Core").assertIsDisplayed()
        composeRule.onNodeWithText("30%").assertIsDisplayed()
        composeRule.onNodeWithText("Purple = assigned load • Green/Red = completed load").assertIsDisplayed()
    }
}

@Composable
private fun TestTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}