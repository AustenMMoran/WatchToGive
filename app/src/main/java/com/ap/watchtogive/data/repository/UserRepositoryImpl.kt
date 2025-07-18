package com.ap.watchtogive.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ap.watchtogive.model.UserStatistics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>
) : UserRepository {

    companion object {
        val TOTAL_ADS_WATCHED_KEY = intPreferencesKey("total_ads_watched")
    }

    override fun getUserStatistics(uid: String): Flow<UserStatistics> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserStatisticsAnon(): Flow<UserStatistics> {
        return dataStore.data.map { prefs ->
            val totalAds = prefs[TOTAL_ADS_WATCHED_KEY] ?: 0
            UserStatistics(totalWatchedAds = totalAds)
        }
    }


    override suspend fun incrementAdWatchCount() {
        TODO("Not yet implemented")
    }

    override suspend fun incrementAdWatchCountAnon() {
        Log.d("lollipop", "incrementAdWatchCountAnon: ")
        // Read current value, increment and save back
        val currentCount = dataStore.data.first()[TOTAL_ADS_WATCHED_KEY] ?: 0
        val newCount = currentCount + 1

        dataStore.edit { prefs ->
            prefs[TOTAL_ADS_WATCHED_KEY] = newCount
        }
    }

    override suspend fun saveUserSelectedCharity(charityId: String) {
        TODO("Not yet implemented")
    }

}

