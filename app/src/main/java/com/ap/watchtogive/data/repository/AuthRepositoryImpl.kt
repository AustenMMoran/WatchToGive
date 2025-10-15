package com.ap.watchtogive.data.repository

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ap.watchtogive.model.AuthState
import com.ap.watchtogive.model.UserData
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _authState.value = mapFirebaseUserToAuthState(auth.currentUser)
        }
    }

    override suspend fun loginAnon() {
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously().await()
        }
    }

    private fun mapFirebaseUserToAuthState(user: FirebaseUser?): AuthState {
        if (user == null) return AuthState.NoLogInDetails

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
            Log.d("lollipop", "User is Known: ${user.uid} Provider: ${user.providerData}")
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

    override suspend fun signInOrLinkWithCredential(credential: Credential) {
        try {
            val firebaseCredential = credential.toFirebaseAuthCredential()
                ?: throw IllegalArgumentException("Unsupported credential type")

            val currentUser = firebaseAuth.currentUser
            if (currentUser != null && currentUser.isAnonymous) {
                try {
                    currentUser.linkWithCredential(firebaseCredential).await()
                } catch (e: FirebaseAuthUserCollisionException) {
                    firebaseAuth.signInWithCredential(firebaseCredential).await()
                }
            } else {
                firebaseAuth.signInWithCredential(firebaseCredential).await()
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign-in failed: ${e.localizedMessage}")
        }
    }

    override suspend fun signOut() {
        val user = firebaseAuth.currentUser ?: return  // If no user, just return
        val isAnon = user.isAnonymous

        try {
            if (isAnon) {
                // Delete user data in DataStore
                dataStore.edit { prefs ->
                    prefs.clear()
                }

                // Delete user from Firebase Auth (anonymous user)
                user.delete().await()
            } else {
                firebaseAuth.signOut()
                _authState.value = AuthState.Idle
            }
        } catch (e: Exception) {
            // Handle any errors here (log, update UI state, etc)
            Log.e("AuthRepository", "Error during signOut: ${e.localizedMessage}")
        } finally {
            // Sign out from Firebase Auth (always sign out)
            firebaseAuth.signOut()
            _authState.value = AuthState.Idle
        }
    }

    private fun Credential.toFirebaseAuthCredential(): AuthCredential? {
        return when (this) {
            is CustomCredential -> when (type) {
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(data)
                    GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                }
                else -> null
            }
            else -> null
        }
    }
}