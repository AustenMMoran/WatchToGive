package com.ap.watchtogive.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationRepository {

    companion object {
        private val LOCATION_KEY = stringPreferencesKey("location_key")
    }

    override fun getLocation(): Flow<String?> = context.dataStore.data.map {
            prefs ->
        prefs[LOCATION_KEY]
    }

    override suspend fun setLocation(newLocation: String) {
        context.dataStore.edit { prefs ->
            prefs[LOCATION_KEY] = newLocation
        }
    }
}
