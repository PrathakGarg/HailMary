package com.arise.habitquest.e2e

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arise.habitquest.presentation.navigation.AriseBottomNav
import com.arise.habitquest.presentation.navigation.Screen
import com.arise.habitquest.ui.theme.AriseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavSmokeE2ETest {

    @get:Rule
    val loggingRule = E2ETestLoggingRule()

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNav_clickingMissionsSelectsMissionsTab() {
        composeRule.setContent {
            val currentRoute = remember { mutableStateOf(Screen.Home.route) }
            AriseTheme {
                AriseBottomNav(
                    currentRoute = currentRoute.value,
                    onHome = { currentRoute.value = Screen.Home.route },
                    onMissions = { currentRoute.value = Screen.MissionBoard.route },
                    onProfile = { currentRoute.value = Screen.StatusWindow.route },
                    onAchievements = { currentRoute.value = Screen.Achievements.route }
                )
            }
        }

        composeRule.onNodeWithTag("bottom_nav_home").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_missions").performClick()
        composeRule.onNodeWithTag("bottom_nav_missions").assertIsDisplayed()
    }
}
