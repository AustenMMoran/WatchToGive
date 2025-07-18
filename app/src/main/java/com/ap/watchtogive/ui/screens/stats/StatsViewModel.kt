package com.ap.watchtogive.ui.screens.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.data.repository.UserRepository
import com.ap.watchtogive.model.AuthState
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<StatsScreenState>(StatsScreenState.Loading)
    val uiState: StateFlow<StatsScreenState> = _uiState.asStateFlow()
    private var statsJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.LoggedInAnon -> {
                        Log.d("lollipop", "AuthState.LoggedInAnon")

                        statsJob = launch {
                            userRepository.getUserStatisticsAnon().collectLatest { stats ->
                                Log.d("lollipop", "stat: $stats")
                                _uiState.value = StatsScreenState.LoggedInAnon(stats)
                            }
                        }
                    }

                    else -> {
                        statsJob = null
                        _uiState.value = StatsScreenState.Loading // or appropriate state
                    }
                }
            }
        }
    }

    val currentUid: String?
        get() = when (val state = authRepository.authState.value) {
            is AuthState.LoggedIn -> state.user?.uid
            is AuthState.LoggedInAnon -> state.user?.uid
            else -> null
        }

    fun linkAccount(){

        val credential = GoogleAuthProvider.getCredential(currentUid, null)

        viewModelScope.launch {
            authRepository.linkAccount(credential)
        }
    }


}