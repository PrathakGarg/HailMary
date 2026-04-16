package com.arise.habitquest.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.ProgressionPreference
import com.arise.habitquest.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsProgressionControlsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun progressionProfileSection_trackAndPreferenceCallbacks_fireOnClick() {
        var selectedTrack: MissionCategory? = null
        var selectedPreference: ProgressionPreference? = null

        composeRule.setContent {
            MaterialTheme {
                ProgressionProfileSection(
                    profile = UserProfile(hunterName = "tester"),
                    onSetTrackFocus = { selectedTrack = it },
                    onSetProgressionPreference = { selectedPreference = it },
                    onSetShoulderRiskFlag = {},
                    onSetHeatRiskFlag = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_track_focus_productivity").performClick()
        composeRule.onNodeWithTag("settings_progression_pref_aggressive").performClick()

        assertEquals(MissionCategory.PRODUCTIVITY, selectedTrack)
        assertEquals(ProgressionPreference.AGGRESSIVE, selectedPreference)
    }

    @Test
    fun progressionProfileSection_riskSwitchCallbacks_fireOnToggle() {
        var shoulderRiskValue: Boolean? = null
        var heatRiskValue: Boolean? = null

        composeRule.setContent {
            MaterialTheme {
                ProgressionProfileSection(
                    profile = UserProfile(
                        hunterName = "tester",
                        shoulderRiskFlag = false,
                        heatRiskFlag = false
                    ),
                    onSetTrackFocus = {},
                    onSetProgressionPreference = {},
                    onSetShoulderRiskFlag = { shoulderRiskValue = it },
                    onSetHeatRiskFlag = { heatRiskValue = it }
                )
            }
        }

        composeRule.onNodeWithTag("settings_shoulder_risk").performClick()
        composeRule.onNodeWithTag("settings_heat_risk").performClick()

        assertTrue(shoulderRiskValue == true)
        assertTrue(heatRiskValue == true)
    }
}
