package com.ap.watchtogive.ui.screens.watch

import android.R.color
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ap.watchtogive.model.AdState
import com.ap.watchtogive.model.StreakState
import com.ap.watchtogive.ui.components.StreakBrokenDialog

@Composable
fun WatchScreen(
    viewModel: WatchScreenViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current

    val playButtonBackground = if (uiState.adLoadState is AdState.Loaded) {
        colorResource(id = color.holo_red_light)
    } else {
        Color.Gray
    }

    if (uiState.showBrokenStreakDialog) {
        StreakBrokenDialog {
            viewModel.acknowledgedBrokenStreak()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,

    ) {

        // Todo turn into a screen animation for updating streak, actual streak info in stat screen
        Text(
            modifier = Modifier,
            text = when (val streak = uiState.currentDailyStreak) {
                is StreakState.Started -> "Streak started! Count: ${streak.streakCount}"
                is StreakState.Continued -> "Streak continued! Count: ${streak.streakCount}"
                is StreakState.NoChange -> "No change. Count: ${streak.streakCount}"
                is StreakState.Broken -> "Streak broken"
                else -> {"No Data"}
            }
        )

        IconButton(
            modifier = Modifier
                .background(playButtonBackground),
            onClick = {
                if (uiState.adLoadState is AdState.Loaded){
                    viewModel.showAd(activity = activity)
                } else {
                    Toast.makeText(context,
                        context.getString(com.ap.watchtogive.R.string.add_still_loading),Toast.LENGTH_SHORT).show()
                }
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
