package com.ap.watchtogive.data.repository

import com.ap.watchtogive.model.StreakState
import com.ap.watchtogive.model.UserData
import com.ap.watchtogive.model.UserStatistics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UserRepository {
    fun getUserStatistics(uid: String): Flow<UserStatistics>
    suspend fun getUserStatisticsAnon(): Flow<UserStatistics>
    suspend fun getUserStatistics(userData: UserData): Flow<UserStatistics>
    suspend fun getCurrentStreakState(uid: String): StreakState
    suspend fun incrementAdWatchCount(): StreakState
    suspend fun incrementAdWatchCountAnon()
    suspend fun saveUserSelectedCharity(charityId: String)
    suspend fun resetUsersStreak(uid: String)
}