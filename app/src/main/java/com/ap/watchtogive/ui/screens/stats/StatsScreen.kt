package com.ap.watchtogive.ui.screens.stats

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ap.watchtogive.model.UserStatistics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
    val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val response = IdpResponse.fromResultIntent(result.data)
            if (response?.error == null) {
                Log.i("lollipop", "Sign-in success!")
            } else {
                Log.e("lollipop", "Sign-in error: ${response.error?.errorCode}, ${response.error?.message}")
            }

        }
    }

    when (uiState) {
        StatsScreenState.Loading -> LoadingView()
        is StatsScreenState.Error -> ErrorView(message = (uiState as StatsScreenState.Error).message)
        is StatsScreenState.LoggedInAnon -> PlaceHolderAnon((uiState as StatsScreenState.LoggedInAnon).stats,
            onLinkAccount = {
                launcher.launch(signInIntent)
            },

            onSignOut = {
                viewModel.signOut()
            }
        )
        is StatsScreenState.LoggedIn -> PlaceHolder((uiState as StatsScreenState.LoggedIn).stats)
    }

}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun PlaceHolder(stats: UserStatistics) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "STATS SCREEN: ${stats.totalWatchedAds}", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun PlaceHolderAnon(
    stats: UserStatistics,
    onLinkAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Optional padding for better spacing
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ANON STATS SCREEN: ${stats.totalWatchedAds}",
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp)) // space between text and button
        Button(onClick = onLinkAccount) {
            Text("Link Google Account")
        }
        Spacer(modifier = Modifier.height(16.dp)) // space between text and button
        Button(onClick = { showSignOutDialog = true }) {
            Text("Sign Out")
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Confirm Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onSignOut()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

