package com.ap.watchtogive

sealed class MainUiState {
    object AuthLoading : MainUiState()
    object NoLoginDetails : MainUiState()
    object ReadyAsGuest : MainUiState()
    object Ready : MainUiState()
    data class Error(val message: String) : MainUiState()

}
