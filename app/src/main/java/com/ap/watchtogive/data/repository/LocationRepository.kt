package com.ap.watchtogive.data.repository

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocation(): Flow<String?>
    suspend fun setLocation(newLocation: String)
}

