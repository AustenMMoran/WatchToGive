package com.ap.watchtogive.ui.screens.select

import com.ap.watchtogive.model.Charity

data class SelectScreenState(
    val isLoading: Boolean = false,
    val charities: List<Charity> = emptyList(),
    val error: String? = null
)