package com.ap.watchtogive.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ap.watchtogive.ui.screens.CharitySelectScreen

sealed class Screen(val route: String) {
    object CharitySelect : Screen("charity_select")
    // Add more screens here as needed
}

@Composable
fun NavHost(
    navController: NavHostController,
    startDestination: String = Screen.CharitySelect.route,
    modifier: Modifier
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.CharitySelect.route) {
            CharitySelectScreen()
        }
    }
}