package com.ap.watchtogive.ui.screens.watch

import com.ap.watchtogive.model.AdState
import com.ap.watchtogive.model.StreakState

/**
 * States will have to include:
 *  - if a charity is selected or not,
 *  - is a user has watched all 5 ads today
 *  @property adLoadState: Representation of current ad's loading state
 */
data class WatchScreenState(
    val selectedCharityId: String? = null,
    val adsWatchedToday: Int = 0,
    val adLoadState: AdState = AdState.Loading,

    //Streak States
    val currentStreakState: StreakState? = null,
    val showBrokenStreakDialog: Boolean = false,
)
