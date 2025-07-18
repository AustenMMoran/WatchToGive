package com.ap.watchtogive.ui.screens.stats

import com.ap.watchtogive.model.UserData
import com.ap.watchtogive.model.UserStatistics

sealed class StatsScreenState {
    object Loading : StatsScreenState()
    data class LoggedIn(val stats: UserStatistics) : StatsScreenState()
    data class LoggedInAnon(val stats: UserStatistics) : StatsScreenState()
    data class Error(val message: String) : StatsScreenState()
}