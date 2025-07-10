package com.ap.watchtogive.ui.screens

import com.ap.watchtogive.model.Charity

data class CharitiesScreenState(
    val isLoading: Boolean = false,
    val charities: List<Charity> = emptyList(),
    val error: String? = null
)