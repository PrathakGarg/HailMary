package com.arise.habitquest.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object RegistrationComplete : Screen("registration_complete")
    object Home : Screen("home")
    object MissionBoard : Screen("mission_board")
    object MissionDetail : Screen("mission_detail/{missionId}") {
        fun createRoute(missionId: String) = "mission_detail/$missionId"
    }
    object MissionComplete : Screen("mission_complete/{missionId}") {
        fun createRoute(missionId: String) = "mission_complete/$missionId"
    }
    object RankUp : Screen("rank_up/{rankName}") {
        fun createRoute(rankName: String) = "rank_up/$rankName"
    }
    object StatusWindow : Screen("status_window")
    object Achievements : Screen("achievements")
    object History : Screen("history")
    object Settings : Screen("settings")
}
