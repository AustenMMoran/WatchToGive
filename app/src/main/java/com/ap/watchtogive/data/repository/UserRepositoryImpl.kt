package com.ap.watchtogive.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ap.watchtogive.data.constants.FirestorePaths
import com.ap.watchtogive.model.StreakState
import com.ap.watchtogive.model.UserData
import com.ap.watchtogive.model.UserStatistics
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
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

            UserStatistics(
                totalWatchedAds = totalAds,
                currentStreak = null
            )
        }
    }

    override suspend fun getUserStatistics(userData: UserData): Flow<UserStatistics> = callbackFlow {
        val docRef = firestore.collection(FirestorePaths.USERS).document(userData.uid)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val count = snapshot?.getLong(FirestorePaths.USERS_TOTAL_ADS_WATCHED)?.toInt() ?: 0
            val streak = snapshot?.getLong(FirestorePaths.USERS_CURRENT_STREAK)?.toInt()

            trySend(
                UserStatistics(
                    totalWatchedAds = count,
                    currentStreak = streak
                )
            )
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getCurrentStreakState(uid: String): StreakState {
        val docRef = firestore.collection(FirestorePaths.USERS).document(uid)
        val snapshot: DocumentSnapshot = docRef.get().await()

        if (!snapshot.exists()) {
            // No data for user, streak probably just started
            return StreakState.NoStreak
        }

        val timestamp = snapshot.getTimestamp(FirestorePaths.USERS_LAST_WATCHED_DATE)
        if (timestamp == null) {
            // No last watched date, treat as streak started
            return StreakState.NoStreak
        }

        val lastDate = timestamp.toDate().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(lastDate, today).toInt()
        val streakSize = snapshot.getLong(FirestorePaths.USERS_CURRENT_STREAK)?.toInt() ?: return StreakState.ErrorGettingData

        return when {
            daysBetween == 0 -> {
                // Same day, streak continues but no change
                StreakState.NoChange(streakSize)
            }
            daysBetween == 1 -> {
                // Yesterday was last watched, streak continues +1
                StreakState.AtRisk(streakSize)
            }
            else -> {
                // Gap > 1 day, streak broken
                StreakState.Broken
            }
        }
    }

    override suspend fun incrementAdWatchCount(): StreakState {
        val userId = firebaseAuth.currentUser!!.uid
        val userDoc = firestore.collection(FirestorePaths.USERS).document(userId)

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDoc)

            val currentStreak = snapshot.getLong(FirestorePaths.USERS_CURRENT_STREAK) ?: 0
            val lastWatched = snapshot.getTimestamp("lastWatchedDate")
            val today = LocalDate.now()
            val lastDate = lastWatched?.toDate()
                ?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDate()

            val (updatedCount, streakResult) = when {
                lastDate == null -> currentStreak + 1 to StreakState.Started(currentStreak.toInt() + 1)
                lastDate.isBefore(today) -> currentStreak + 1 to StreakState.Continued(currentStreak.toInt() + 1)
                lastDate.isEqual(today) -> currentStreak to StreakState.NoChange(currentStreak.toInt())
                else -> currentStreak to StreakState.NoChange(currentStreak.toInt())
            }


            val updates = mapOf(
                FirestorePaths.USERS_CURRENT_STREAK to updatedCount,
                FirestorePaths.USERS_LAST_WATCHED_DATE to Timestamp.now(),
                FirestorePaths.USERS_TOTAL_ADS_WATCHED to FieldValue.increment(1)
            )

            transaction.set(userDoc, updates, SetOptions.merge())

            // Return streak result from the transaction
            streakResult
        }.addOnFailureListener {
            Log.e("Firestore", "Failed to increment ad watch", it)
        }.await()
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

    override suspend fun resetUsersStreak(uid: String) {
        val userDoc = firestore.collection(FirestorePaths.USERS).document(uid)
        userDoc.update(
            mapOf(
                FirestorePaths.USERS_LAST_WATCHED_DATE to FieldValue.delete(),
                FirestorePaths.USERS_CURRENT_STREAK to FieldValue.delete()
            )
        )
    }

}

