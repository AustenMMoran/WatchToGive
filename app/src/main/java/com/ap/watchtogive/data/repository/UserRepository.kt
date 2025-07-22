package com.ap.watchtogive.data.repository

import com.ap.watchtogive.model.UserData
import com.ap.watchtogive.model.UserStatistics
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserStatistics(uid: String): Flow<UserStatistics>
    suspend fun getUserStatisticsAnon(): Flow<UserStatistics>
    suspend fun getUserStatistics(userData:UserData): Flow<UserStatistics>
    suspend fun incrementAdWatchCount()
    suspend fun incrementAdWatchCountAnon()
    suspend fun saveUserSelectedCharity(charityId: String)
}