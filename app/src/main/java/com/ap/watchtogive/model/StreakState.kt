package com.ap.watchtogive.model

sealed class StreakState {
    object NoStreak : StreakState()
    data class NoChange(val streakCount: Int) : StreakState()
    data class AtRisk(val streakCount: Int) : StreakState()
    data class Started(val streakCount: Int) : StreakState()
    data class Continued(val streakCount: Int) : StreakState()
    object Broken : StreakState()
    object ErrorGettingData : StreakState()
}
