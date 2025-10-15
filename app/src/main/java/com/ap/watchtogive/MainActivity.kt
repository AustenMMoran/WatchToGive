package com.ap.watchtogive

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ap.watchtogive.ui.components.ChooseAccountType
import com.ap.watchtogive.ui.components.ErrorScreen
import com.ap.watchtogive.ui.components.SplashScreen
import com.ap.watchtogive.ui.navigation.BottomNavigation
import com.ap.watchtogive.ui.navigation.NavHost
import com.ap.watchtogive.ui.navigation.Screen
import com.ap.watchtogive.ui.theme.WatchToGiveTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            // Build the Credential Manager request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(this)
            val coroutineScope = rememberCoroutineScope()

            val addAccountLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->

                Log.d("test-auth", "create google account [result]: ${result.resultCode}")
                // This callback triggers when the user returns from the "Add Account" screen
                coroutineScope.launch {
                    try {
                        val response = credentialManager.getCredential(
                            context = this@MainActivity,
                            request = request
                        )
                        mainViewModel.signInWithCredential(response.credential)
                    } catch (e: Exception) {
                        // Still no credentials → show fallback UI
                        Log.d("test-auth", "onAccountLauncher: STILL No credentials - return to default")
                        mainViewModel.setUiState(MainUiState.NoLoginDetails)
                    }
                }
            }

            LaunchedEffect(mainUiState) {
                if (mainUiState is MainUiState.NoLoginDetails && navController.currentDestination != null) {
                    navController.navigate(Screen.CharityWatch.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            WatchToGiveTheme {
                when (mainUiState) {
                    is MainUiState.GoogleSignIn -> {
                        val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                        }
                        addAccountLauncher.launch(intent)

                        mainViewModel.setUiState(MainUiState.AuthLoading)
                    }
                    is MainUiState.AuthLoading -> {
                        SplashScreen()
                    }

                    is MainUiState.NoLoginDetails -> {

                        ChooseAccountType(
                            onContinueAsGuest = {
                                mainViewModel.setUiState(MainUiState.AuthLoading)
                                mainViewModel.continueAsGuest()
                            },
                            onGoogleSignIn = {
                                mainViewModel.setUiState(MainUiState.AuthLoading)
                                coroutineScope.launch {
                                    try {
                                        val response = credentialManager.getCredential(
                                            context = this@MainActivity,
                                            request = request
                                        )

                                        mainViewModel.signInWithCredential(response.credential)

                                    } catch (e: Exception) {
                                        Log.e("test-auth", "sign in: [catch] ${e.message}")

                                        if (e.message?.contains("No credentials available") == true) {
                                            // FALLBACK: show sign-in chooser UI
                                            Log.d(
                                                "test-auth",
                                                "No credentials, fallback to Google sign-in UI"
                                            )
                                            mainViewModel.setUiState(MainUiState.GoogleSignIn)
                                        } else {
                                            mainViewModel.setUiState(
                                                MainUiState.Error(e.localizedMessage ?: "Google sign-in failed")
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    is MainUiState.Error -> {
                        ErrorScreen(
                            message = (mainUiState as MainUiState.Error).message,
                            onRetry = {
                                mainViewModel.setUiState(MainUiState.NoLoginDetails)
                            }
                        )
                    }

                    is MainUiState.ReadyAsGuest,
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


/*

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

            val googleCredentialLauncher = rememberLauncherForActivityResult(
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
                    mainViewModel.setUiState(MainUiState.Error(message = result.resultCode.toString()))

                }
            }

            val googleCreateAccountLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                mainViewModel.setUiState(MainUiState.NoLoginDetails)
            }

 */