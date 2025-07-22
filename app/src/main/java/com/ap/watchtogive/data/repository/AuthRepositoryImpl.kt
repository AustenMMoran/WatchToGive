package com.ap.watchtogive.data.repository

import android.util.Log
import com.ap.watchtogive.data.constants.FirestorePaths
import com.ap.watchtogive.model.AuthState
import com.ap.watchtogive.model.UserData
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

    override suspend fun signInOrLinkWithGoogleIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = firebaseAuth.currentUser

        try {
            if (currentUser != null && currentUser.isAnonymous) {
                currentUser.linkWithCredential(credential).await()
            } else {
                firebaseAuth.signInWithCredential(credential).await()
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            firebaseAuth.signInWithCredential(credential).await()
        }
    }

    override suspend fun signOut() {
        val user = firebaseAuth.currentUser ?: return  // If no user, just return
        val isAnon = user.isAnonymous

        try {
            if (isAnon) {
                // Delete user data in Firestore
                firestore.collection(FirestorePaths.USERS).document(user.uid).delete().await()

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

}