package com.ap.watchtogive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.MainUiState.*
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.AuthLoading)
    val mainUiState: StateFlow<MainUiState> = _mainUiState

    init {
        viewModelScope.launch {
            authRepository.authState
                .collect { state ->
                Log.d("lollipop", "AUTH STATE MVM = $state")
                _mainUiState.value = when (state) {
                    is AuthState.Idle -> AuthLoading
                    is AuthState.NoLogInDetails -> NoLoginDetails
                    is AuthState.LoggedIn -> Ready
                    is AuthState.LoggedInAnon -> ReadyAsGuest
                    is AuthState.Error -> Error(state.message)
                }
            }
        }

    }

    fun continueAsGuest(){
        viewModelScope.launch {
            authRepository.loginAnon()
        }
    }

    fun loginWithToken(idToken: String) {
        viewModelScope.launch {
           authRepository.signInOrLinkWithGoogleIdToken(idToken)
        }
    }

    fun setUiState(state: MainUiState){
        _mainUiState.value = state
    }
}