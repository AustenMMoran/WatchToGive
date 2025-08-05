package com.ap.watchtogive.data.repository

import com.ap.watchtogive.model.AuthState
import com.ap.watchtogive.model.StreakState
import com.ap.watchtogive.model.UserData
import com.ap.watchtogive.model.UserStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface UserRepository {
    val currentStreakState: StateFlow<StreakState>
    fun getCurrentStreak(uid: String)
    fun getUserStatistics(uid: String): Flow<UserStatistics>
    suspend fun getUserStatisticsAnon(): Flow<UserStatistics>
    suspend fun getUserStatistics(userData: UserData): Flow<UserStatistics>
    fun incrementAdWatchCount()
    suspend fun incrementAdWatchCountAnon()
    suspend fun saveUserSelectedCharity(charityId: String)
    suspend fun resetUsersStreak(uid: String)
}