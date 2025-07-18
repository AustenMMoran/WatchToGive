package com.ap.watchtogive.data.repository

import com.ap.watchtogive.model.AuthProvider
import com.ap.watchtogive.model.AuthState
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun loginAnon()
    suspend fun logout()

    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun linkAccount(
        credential: AuthCredential
    )

}
