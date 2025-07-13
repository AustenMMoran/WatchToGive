package com.ap.watchtogive.ui.screens.watch

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.AdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchScreenViewModel
    @Inject
    constructor(
        private val adsRepository: AdsRepository,
    ) : ViewModel() {
        // Todo: Implement UI screen state with datastore saving the user's progress? Honestly probs better firebase with auth (ps need an authscreen state i guess)
        private val _uiState = MutableStateFlow(WatchScreenState())
        val uiState: StateFlow<WatchScreenState> = _uiState.asStateFlow()

        // todo: remember to set loadstate.error -> loadadd()
        init {
            adsRepository.loadAd()
            viewModelScope.launch {
                adsRepository.adLoadState.collect { adState ->
                    _uiState.value = _uiState.value.copy(adLoadState = adState)
                }
            }
        }

        fun showAd(
            activity: Activity?,
            onAdFinished: () -> Unit,
        ) {
            if (activity != null) {
                adsRepository.showAd(
                    activity = activity,
                    onAdFinished = {
                        adsRepository.loadAd()
                        onAdFinished()
                    },
                )
            } else {
                Log.e(TAG, "showAd: Null Activity")
            }
        }

        fun isAdReady(): Boolean = adsRepository.isAdAvailable()
    }
