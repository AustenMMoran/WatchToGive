package com.ap.watchtogive.model

sealed class StreakState {
    data class Started(val streakCount: Int) : StreakState()
    data class Continued(val streakCount: Int) : StreakState()
    data class NoChange(val streakCount: Int) : StreakState()
    object Broken : StreakState()
}
