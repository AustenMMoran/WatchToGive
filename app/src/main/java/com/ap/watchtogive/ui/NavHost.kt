package com.ap.watchtogive.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ap.watchtogive.ui.screens.select.SelectScreen
import com.ap.watchtogive.ui.screens.stats.StatsScreen
import com.ap.watchtogive.ui.screens.watch.WatchScreen

sealed class Screen(val route: String) {
    object CharityStats : Screen("charity_stats")
    object CharityWatch : Screen("charity_watch")
    object CharitySelect : Screen("charity_select")
}

@Composable
fun NavHost(
    navController: NavHostController,
    startDestination: String = Screen.CharitySelect.route,
    modifier: Modifier,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.CharityStats.route) {
            StatsScreen()
        }
        composable(Screen.CharityWatch.route) {
            WatchScreen()
        }
        composable(Screen.CharitySelect.route) {
            SelectScreen()
        }
    }
}
