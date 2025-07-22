package com.ap.watchtogive.model

sealed class AuthState {
    object Idle: AuthState() {
        override fun toString() = "Idle"
    }

    object NoLogInDetails: AuthState() {
        override fun toString() = "NoLogInDetails"
    }

    data class LoggedIn(
        val user: UserData? = null
    ): AuthState()

    data class LoggedInAnon(
        val user: UserData? = null
    ): AuthState()

    data class Error(
        val message: String
    ): AuthState()

}