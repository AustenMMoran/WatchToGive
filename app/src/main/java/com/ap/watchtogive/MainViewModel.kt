package com.ap.watchtogive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.model.AuthProvider
import com.ap.watchtogive.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
                _mainUiState.value = when (state) {
                    is AuthState.Idle -> MainUiState.Loading
                    is AuthState.LoggedIn -> MainUiState.Ready
                    is AuthState.LoggedInAnon -> MainUiState.Ready
                    is AuthState.Error -> MainUiState.Error(state.message)
                }
            }
        }

        // Trigger login (could be automatic anonymous sign-in)
        viewModelScope.launch {
            authRepository.login()
        }
    }

    fun retryAuth(){
        viewModelScope.launch {
            authRepository.login()
        }
    }
}