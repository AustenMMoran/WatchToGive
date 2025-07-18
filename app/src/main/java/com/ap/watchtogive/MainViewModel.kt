package com.ap.watchtogive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.model.AuthState
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel initiates early authentication logic on app startup,
 * exposing the user's login state (e.g., isLoggedIn) through the AuthRepository.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel(){

    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val mainUiState: StateFlow<MainUiState> = _mainUiState


    init {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                Log.d("lollipop", "AUTH STATE MVM = $state")
                _mainUiState.value = when (state) {
                    is AuthState.Idle -> MainUiState.Loading
                    is AuthState.LoggedIn -> MainUiState.Ready
                    is AuthState.LoggedInAnon -> MainUiState.ReadyAsGuest
                    is AuthState.Error -> MainUiState.Error(state.message)
                }
            }
        }

        // Trigger login (could be automatic anonymous sign-in)
        viewModelScope.launch {
            authRepository.loginAnon()
        }
    }

    fun retryAuth(){
        viewModelScope.launch {
            authRepository.loginAnon()
        }
    }

    fun continueAsGuest(){
        _mainUiState.value = MainUiState.Ready
    }
    fun setLoading(){
        _mainUiState.value = MainUiState.Loading
    }

    fun loginWithToken(idToken: String) {
        viewModelScope.launch {
           authRepository.signInOrLinkWithGoogleIdToken(idToken)
        }
    }
}