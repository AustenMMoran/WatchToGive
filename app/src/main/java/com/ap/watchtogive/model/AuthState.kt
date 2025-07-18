package com.ap.watchtogive.model

sealed class AuthState {
    object Idle : AuthState()

    data class LoggedIn(
        val user: UserData? = null
    ) : AuthState()

    data class LoggedInAnon(
        val user: UserData? = null
    ) : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}