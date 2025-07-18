package com.ap.watchtogive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ap.watchtogive.ui.components.ErrorScreen
import com.ap.watchtogive.ui.components.SplashScreen
import com.ap.watchtogive.ui.navigation.BottomNavigation
import com.ap.watchtogive.ui.navigation.NavHost
import com.ap.watchtogive.ui.navigation.Screen
import com.ap.watchtogive.ui.theme.WatchToGiveTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val mainUiState by mainViewModel.mainUiState.collectAsState()

            WatchToGiveTheme {
                when (mainUiState) {
                    is MainUiState.AuthLoading -> {
                        SplashScreen()
                    }
                    is MainUiState.Error -> {
                        ErrorScreen(
                            message = (mainUiState as MainUiState.Error).message,
                            onRetry = {
                                mainViewModel.retryAuth()
                            }
                        )
                    }
                    is MainUiState.Ready -> {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        enableEdgeToEdge()

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                BottomNavigation(
                                    currentRoute,
                                    onNavSelected = { newRoute ->
                                        if (newRoute != currentRoute) {
                                            navController.navigate(newRoute) {
                                                // Optional: popUpTo, launchSingleTop, etc.
                                            }
                                        }
                                    },
                                )
                            },
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.CharityWatch.route,
                                modifier = Modifier.padding(innerPadding),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Todo: Next is make a nav bar like on figma.
