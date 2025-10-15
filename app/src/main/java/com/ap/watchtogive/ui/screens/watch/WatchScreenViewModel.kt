package com.ap.watchtogive.ui.screens.watch

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.AdsRepository
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.data.repository.UserRepository
import com.ap.watchtogive.model.AuthState
import com.ap.watchtogive.model.StreakState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchScreenViewModel @Inject constructor(
    private val adsRepository: AdsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WatchScreenState())
    val uiState: StateFlow<WatchScreenState> = _uiState.asStateFlow()
    private var streakJob: Job? = null


    // todo: remember to set loadstate.error -> loadadd()
    init {
        // Load ad + manage state
        adsRepository.loadAd()
        viewModelScope.launch {
            adsRepository.adLoadState.collect { adState ->
                Log.d("lollipop", "Watch Screen state: $adState")
                _uiState.value = _uiState.value.copy(adLoadState = adState)
            }
        }


        // Consider user's streak
        viewModelScope.launch {
            authRepository.authState.collectLatest { state ->
                streakJob?.cancel()  // Cancel previous collection if any
                if (state is AuthState.LoggedIn) {
                    userRepository.getCurrentStreak(state.user.uid)
                    streakJob = launch {
                        userRepository.currentStreakState.collect { streakState ->
                            handleStreakState(streakState, state.user.uid)
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleStreakState(streakState: StreakState, uid: String) {
        Log.d("lollipop", "StreakState Updated: $streakState")
        when (streakState) {
            StreakState.Broken -> {
                userRepository.resetUsersStreak(uid)
                _uiState.update {
                    it.copy(
                        showBrokenStreakDialog = true,
                        currentStreakState = streakState
                    )
                }
            }
            is StreakState.AtRisk,
            is StreakState.Started,
            is StreakState.Continued,
            is StreakState.NoChange -> {
                // Todo: Consider UI flairs, celebration
                _uiState.update {
                    it.copy(currentStreakState = streakState)
                }
            }
            StreakState.NoStreak,
            StreakState.ErrorGettingData -> {
                // Optional UI updates
            }
        }
    }

    fun showAd(
        activity: Activity?,
    ) {
        if (activity != null) {
            adsRepository.showAd(
                activity = activity,
                onAdFinished = {
                    adsRepository.loadAd()
                    updateUserStatistics()
                },
            )
        } else {
            Log.e(TAG, "showAd: Null Activity")
        }
    }

    fun updateUserStatistics() {
        val currentAuthState = authRepository.authState.value
        when (currentAuthState) {
            is AuthState.LoggedInAnon -> {
                viewModelScope.launch {
                    userRepository.incrementAdWatchCountAnon()
                }
            }

            is AuthState.LoggedIn -> {
                viewModelScope.launch {
                    userRepository.incrementAdWatchCount()
                }
            }

            else -> {
                Log.e(TAG, "updateUserStatistics: No account available")
            }
        }
    }

    fun acknowledgedBrokenStreak() {
        _uiState.update {
            it.copy(showBrokenStreakDialog = false)
        }
    }
}