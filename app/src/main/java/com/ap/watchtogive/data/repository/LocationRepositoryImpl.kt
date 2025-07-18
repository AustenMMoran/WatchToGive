package com.ap.watchtogive.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.Preferences

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : LocationRepository {
    companion object {
        private val LOCATION_KEY = stringPreferencesKey("location_key")
    }

    override fun getLocation(): Flow<String?> =
        dataStore.data.map { prefs ->
            prefs[LOCATION_KEY]
        }

    override suspend fun setLocation(newLocation: String) {
        dataStore.edit { prefs ->
            prefs[LOCATION_KEY] = newLocation
        }
    }
}
