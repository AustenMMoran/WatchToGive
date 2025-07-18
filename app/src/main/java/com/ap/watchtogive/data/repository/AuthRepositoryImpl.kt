package com.ap.watchtogive.data.repository

import android.util.Log
import com.ap.watchtogive.model.AuthProvider
import com.ap.watchtogive.model.AuthState
import com.ap.watchtogive.model.UserData
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun login() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User already signed in (anon or with provider)
            _authState.value = mapFirebaseUserToAuthState(currentUser)
        } else {
            // No user signed in, sign in anonymously
            val authResult = firebaseAuth.signInAnonymously().await()
            _authState.value = mapFirebaseUserToAuthState(authResult.user)
        }
    }

    private fun mapFirebaseUserToAuthState(user: FirebaseUser?): AuthState {
        if (user == null) return AuthState.Idle

        val isAnon = user.isAnonymous
        val userData = UserData(
            uid = user.uid,
            displayName = user.displayName,
            email = user.email
        )

        return if (isAnon) {
            Log.d("lollipop", "User is Anon: ${user.uid}")
            AuthState.LoggedInAnon(user = userData)
        } else {
            Log.d("lollipop", "User is Known: ${user.uid}")
            AuthState.LoggedIn(user = userData)
        }
    }



    override suspend fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
            _authState.value = AuthState.Idle
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Logout failed")
        }
    }

    override suspend fun linkAccount(
        credential: AuthCredential
    ) {
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            // No user signed in at all, maybe prompt sign in instead of link
            _authState.value = AuthState.Error("No authenticated user to link account with")
            return
        }

        try {
            // Try linking the credential to current user (which might be anonymous)
            val authResult = currentUser.linkWithCredential(credential).await()

            val firebaseUser = authResult.user

            val userData = firebaseUser?.run {
                UserData(
                    uid = uid,
                    displayName = displayName,
                    email = email
                )
            }

            _authState.value = AuthState.LoggedIn(
                user = userData
            )
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Error linking account")
        }
    }
}