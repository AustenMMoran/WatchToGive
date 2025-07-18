package com.ap.watchtogive.ui.screens.select

import com.ap.watchtogive.model.Charity

sealed class SelectScreenState {
    object Loading : SelectScreenState()

    data class Success(
        val charities: List<Charity>
    ) : SelectScreenState()

    data class Error(
        val message: String
    ) : SelectScreenState()
}

