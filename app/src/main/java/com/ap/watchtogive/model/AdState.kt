package com.ap.watchtogive.model

sealed class AdState {
    object Idle : AdState()

    object Loading : AdState()

    object Loaded : AdState()

    data class Error(
        val message: String,
    ) : AdState()
}
