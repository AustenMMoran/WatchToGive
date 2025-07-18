package com.ap.watchtogive.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.data.repository.UserRepository
import com.ap.watchtogive.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<StatsScreenState>(StatsScreenState.Loading)
    val uiState: StateFlow<StatsScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.Idle -> {
                        _uiState.value = StatsScreenState.Loading
                    }
                    is AuthState.LoggedIn -> {
                        fetchStats()
                    }
                    is AuthState.LoggedInAnon -> {
                        userRepository.getUserStatisticsAnon()
                            .collect { stats ->
                                _uiState.value = StatsScreenState.LoggedInAnon(stats)
                            }
                    }
                    is AuthState.Error -> {
                        _uiState.value = StatsScreenState.Error(authState.message)
                    }
                }
            }
        }
    }

    fun fetchStats(){
        //Todo: get extensive data
    }

    fun fetchLocalStats(){
        //Todo: get extensive data
    }
}