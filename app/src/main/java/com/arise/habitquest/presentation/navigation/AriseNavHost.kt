package com.arise.habitquest.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arise.habitquest.presentation.achievements.AchievementScreen
import com.arise.habitquest.presentation.complete.MissionCompleteScreen
import com.arise.habitquest.presentation.history.HistoryScreen
import com.arise.habitquest.presentation.home.HomeScreen
import com.arise.habitquest.presentation.missions.MissionBoardScreen
import com.arise.habitquest.presentation.missions.MissionDetailScreen
import com.arise.habitquest.presentation.onboarding.OnboardingScreen
import com.arise.habitquest.presentation.profile.StatusWindowScreen
import com.arise.habitquest.presentation.rankup.RankUpScreen
import com.arise.habitquest.presentation.registration.RegistrationCompleteScreen
import com.arise.habitquest.presentation.settings.SettingsScreen
import com.arise.habitquest.presentation.splash.SplashScreen
import com.arise.habitquest.ui.theme.*

private val TAB_ROUTES = setOf(
    Screen.Home.route,
    Screen.MissionBoard.route,
    Screen.StatusWindow.route,
    Screen.Achievements.route
)

@Composable
fun AriseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Screen.Home.route) { saveState = true }
        }
    }

    Scaffold(
        containerColor = BackgroundDeep,
        bottomBar = {
            if (currentRoute in TAB_ROUTES) {
                AriseBottomNav(
                    currentRoute = currentRoute,
                    onHome        = { navigateToTab(Screen.Home.route) },
                    onMissions    = { navigateToTab(Screen.MissionBoard.route) },
                    onProfile     = { navigateToTab(Screen.StatusWindow.route) },
                    onAchievements = { navigateToTab(Screen.Achievements.route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = modifier
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.RegistrationComplete.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.RegistrationComplete.route) {
                RegistrationCompleteScreen(
                    onEnterApp = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.RegistrationComplete.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToMissions = { navigateToTab(Screen.MissionBoard.route) },
                    onNavigateToMissionDetail = { id ->
                        navController.navigate(Screen.MissionDetail.createRoute(id))
                    },
                    onNavigateToProfile = { navigateToTab(Screen.StatusWindow.route) },
                    onNavigateToAchievements = { navigateToTab(Screen.Achievements.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToRankUp = { rankName ->
                        navController.navigate(Screen.RankUp.createRoute(rankName))
                    },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    bottomBarPadding = innerPadding
                )
            }

            composable(Screen.MissionBoard.route) {
                MissionBoardScreen(
                    onMissionClick = { id ->
                        navController.navigate(Screen.MissionDetail.createRoute(id))
                    },
                    bottomBarPadding = innerPadding
                )
            }

            composable(
                route = Screen.MissionDetail.route,
                arguments = listOf(navArgument("missionId") { type = NavType.StringType })
            ) { backStack ->
                val missionId = backStack.arguments?.getString("missionId") ?: return@composable
                MissionDetailScreen(
                    missionId = missionId,
                    onComplete = { id ->
                        navController.navigate(Screen.MissionComplete.createRoute(id)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MissionComplete.route,
                arguments = listOf(navArgument("missionId") { type = NavType.StringType })
            ) { backStack ->
                val missionId = backStack.arguments?.getString("missionId") ?: return@composable
                MissionCompleteScreen(
                    missionId = missionId,
                    onReturn = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.StatusWindow.route) {
                StatusWindowScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToAchievements = { navigateToTab(Screen.Achievements.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    bottomBarPadding = innerPadding
                )
            }

            composable(Screen.Achievements.route) {
                AchievementScreen(
                    onBack = { navController.popBackStack() },
                    bottomBarPadding = innerPadding
                )
            }


            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.History.route) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.RankUp.route,
                arguments = listOf(navArgument("rankName") { type = NavType.StringType })
            ) { backStack ->
                val rankName = backStack.arguments?.getString("rankName") ?: return@composable
                RankUpScreen(
                    newRankName = rankName,
                    onContinue = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AriseBottomNav(
    currentRoute: String?,
    onHome: () -> Unit,
    onMissions: () -> Unit,
    onProfile: () -> Unit,
    onAchievements: () -> Unit
) {
    NavigationBar(
        containerColor = BackgroundSurface,
        tonalElevation = 0.dp,
        modifier = Modifier.border(BorderStroke(1.dp, BorderDefault))
    ) {
        NavItem("Home",     Icons.Filled.Home,        currentRoute == Screen.Home.route,         onHome)
        NavItem("Missions", Icons.Filled.Menu,         currentRoute == Screen.MissionBoard.route,  onMissions)
        NavItem("Status",   Icons.Filled.Person,       currentRoute == Screen.StatusWindow.route,  onProfile)
        NavItem("Awards",   Icons.Filled.EmojiEvents,  currentRoute == Screen.Achievements.route,  onAchievements)
    }
}

@Composable
fun RowScope.NavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, label, tint = if (selected) PurpleLight else TextSecondary) },
        label = {
            Text(
                label,
                style = AriseTypography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = if (selected) PurpleLight else TextDim
                )
            )
        },
        colors = NavigationBarItemDefaults.colors(indicatorColor = PurpleFaint)
    )
}
