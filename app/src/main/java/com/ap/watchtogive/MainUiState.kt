package com.ap.watchtogive

sealed class MainUiState {
    object AuthLoading : MainUiState()
    object ReadyAsGuest : MainUiState()
    object Ready : MainUiState()
    data class Error(val message: String) : MainUiState()

    companion object {
        val Loading: MainUiState = AuthLoading
    }
}
