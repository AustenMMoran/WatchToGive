package com.ap.watchtogive

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ap.watchtogive.ui.components.ContinueAsGuestScreen
import com.ap.watchtogive.ui.components.ErrorScreen
import com.ap.watchtogive.ui.components.SplashScreen
import com.ap.watchtogive.ui.navigation.BottomNavigation
import com.ap.watchtogive.ui.navigation.NavHost
import com.ap.watchtogive.ui.navigation.Screen
import com.ap.watchtogive.ui.theme.WatchToGiveTheme
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.arrayListOf

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val mainViewModel: MainViewModel = hiltViewModel()
            val mainUiState by mainViewModel.mainUiState.collectAsState()

            val oneTapClient = Identity.getSignInClient(this)
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        // Pass ID token to ViewModel to authenticate with Firebase or backend
                        mainViewModel.loginWithToken(idToken)
                    } else {
                        Log.e("lollipop", "No ID token in credential")
                    }
                } else {
                    Log.e("lollipop", "One Tap Sign-in failed or cancelled")
                }
            }

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
                    is MainUiState.ReadyAsGuest -> {
                        ContinueAsGuestScreen(
                            onContinueAsGuest = {
                                mainViewModel.continueAsGuest()
                            },
                            onSignIn = {
                                Log.d("lollipop", "ContinueAsGuestScreen: onSignIn")
                                mainViewModel.setLoading()
                                oneTapClient.beginSignIn(signInRequest)
                                    .addOnSuccessListener { result ->
                                        try {
                                            launcher.launch(
                                                IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                            )
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Couldn't launch One Tap UI: ${e.localizedMessage}")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MainActivity", "One Tap beginSignIn failed: ${e.localizedMessage}")
                                    }
                            }
                        )
                    }
                    is MainUiState.Ready -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                BottomNavigation(
                                    currentRoute,
                                    onNavSelected = { newRoute ->
                                        if (newRoute != currentRoute) {
                                            navController.navigate(newRoute) {
                                                launchSingleTop = true       // Prevent duplicate destinations
                                                restoreState = true          // Restore saved state (including ViewModel)
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true         // Save state of popped destinations
                                                }
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
