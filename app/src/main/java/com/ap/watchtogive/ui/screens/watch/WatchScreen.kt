package com.ap.watchtogive.ui.screens.watch

import android.R
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ap.watchtogive.model.AdState

@Composable
fun WatchScreen(
    viewModel: WatchScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current

    val playButtonBackground = if (uiState.adLoadState is AdState.Loaded) {
        colorResource(id = R.color.holo_red_light)
    } else {
        Color.Gray
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        IconButton(
            modifier = Modifier
                .background(playButtonBackground),
            onClick = {
                viewModel.showAd(activity = activity)
            }
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White
            )
        }
    }

}