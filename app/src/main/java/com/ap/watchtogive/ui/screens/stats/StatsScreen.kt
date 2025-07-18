package com.ap.watchtogive.ui.screens.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ap.watchtogive.model.UserStatistics

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        StatsScreenState.Loading -> LoadingView()
        is StatsScreenState.Error -> ErrorView(message = (uiState as StatsScreenState.Error).message)
        is StatsScreenState.LoggedInAnon -> PlaceHolderAnon((uiState as StatsScreenState.LoggedInAnon).stats)
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
private fun PlaceHolderAnon(stats: UserStatistics) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "ANON STATS SCREEN: ${stats.totalWatchedAds}", color = MaterialTheme.colorScheme.error)
    }
}
