package com.ap.watchtogive.data.repository

import android.app.Activity
import com.ap.watchtogive.model.AdState
import kotlinx.coroutines.flow.StateFlow

interface AdsRepository {
    val adLoadState: StateFlow<AdState>

    fun loadAd()

    fun showAd(
        activity: Activity,
        onAdFinished: () -> Unit,
    )

    fun isAdAvailable(): Boolean
}
