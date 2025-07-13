package com.ap.watchtogive.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ap.watchtogive.model.AdState
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Todo: replace with real ads: "ca-app-pub-7049640721055375/6722296723"
 */
class AdsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AdsRepository {
    private val tag = "AdsRepositoryImpl"
    private val _adLoadState = MutableStateFlow<AdState>(AdState.Idle)
    override val adLoadState: StateFlow<AdState> = _adLoadState

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    init {
        MobileAds.initialize(context)
    }

    override fun loadAd() {
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        _adLoadState.value = AdState.Loading

        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    scope.launch {
                        Log.d(tag, "Ad loaded and ready")
                        interstitialAd = ad
                        isLoading = false
                        _adLoadState.value = AdState.Loaded
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    scope.launch {
                        Log.e(tag, "Ad failed to load: ${error.message}")
                        interstitialAd = null
                        isLoading = false
                        _adLoadState.value = AdState.Error(error.message)
                    }
                }
            }
        )
    }

    override fun showAd(activity: Activity, onAdFinished: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdFinished()
                    scope.launch {
                        interstitialAd = null
                        loadAd()
                        _adLoadState.value = AdState.Loading
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(
                        tag,
                        "Failed To Show Full Screen Content: ${adError.message}"
                    )
                    onAdFinished()
                    scope.launch {
                        interstitialAd = null
                        _adLoadState.value = AdState.Error(adError.message)
                    }
                }
            }
            ad.show(activity)
        } else {
            onAdFinished()
            scope.launch {
                Log.d(tag, "Ad not ready")
                loadAd()
                _adLoadState.value = AdState.Loading
            }
        }
    }

    override fun isAdAvailable(): Boolean = interstitialAd != null

}