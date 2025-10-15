package com.ap.watchtogive.data.repository

import androidx.credentials.Credential
import com.ap.watchtogive.model.AuthState
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun loginAnon()
    suspend fun logout()
    suspend fun signInOrLinkWithCredential(credential: Credential)
    suspend fun signOut()

}
