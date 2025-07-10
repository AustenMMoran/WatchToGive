package com.ap.watchtogive.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ap.watchtogive.data.repository.CharitiesRepository
import com.ap.watchtogive.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CharitiesViewModel @Inject constructor(
    private val repository: CharitiesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharitiesScreenState(isLoading = true))
    val uiState: StateFlow<CharitiesScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            locationRepository.getLocation()
                .map { it ?: "gb" }  // default if null
                .flatMapLatest { location ->
                    repository.getCharitiesByLocation(location)
                        .onStart {
                            _uiState.value = CharitiesScreenState(isLoading = true)
                        }
                }
                .catch { e ->
                    _uiState.value = CharitiesScreenState(error = e.message ?: "Unknown error")
                }
                .collect { charities ->
                    _uiState.value = CharitiesScreenState(isLoading = false, charities = charities)
                }
        }
    }




}

