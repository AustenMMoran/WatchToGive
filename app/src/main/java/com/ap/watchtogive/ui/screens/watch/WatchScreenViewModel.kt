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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchScreenViewModel
    @Inject
    constructor(
        private val adsRepository: AdsRepository,
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchScreenState())
        val  uiState: StateFlow<WatchScreenState> = _uiState.asStateFlow()


    // todo: remember to set loadstate.error -> loadadd()
        init {
            adsRepository.loadAd()
            viewModelScope.launch {
                adsRepository.adLoadState.collect { adState ->
                    Log.d("lollipop", "Watch Screen state: $adState")
                    _uiState.value = _uiState.value.copy(adLoadState = adState)
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
                    val result = userRepository.incrementAdWatchCount()
                    _uiState.update { currentState ->
                        currentState.copy(currentDailyStreak = result)
                    }
                }
            }
            else -> {
                Log.e(TAG, "updateUserStatistics: No account available", )
            }
        }
    }


        fun isAdReady(): Boolean = adsRepository.isAdAvailable()
    }
