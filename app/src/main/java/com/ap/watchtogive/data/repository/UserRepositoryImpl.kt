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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val dataStore: DataStore<Preferences>,
) : UserRepository {

    private val _currentStreakState = MutableStateFlow<StreakState>(StreakState.NoStreak)
    override val currentStreakState: StateFlow<StreakState> = _currentStreakState
    private var listenerRegistration: ListenerRegistration? = null
    private var lastCachedData: Pair<Timestamp?, Int?>? = null

    companion object {
        val TOTAL_ADS_WATCHED_KEY = intPreferencesKey("total_ads_watched")
    }

    override fun getCurrentStreak(uid: String) {
        val docRef = firestore.collection(FirestorePaths.USERS).document(uid)
        listenerRegistration?.remove() // Remove old listener

        listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                _currentStreakState.value = StreakState.ErrorGettingData
                return@addSnapshotListener
            }

            val timestamp = snapshot.getTimestamp(FirestorePaths.USERS_LAST_WATCHED_TIMESTAMP)
            val streakCount = snapshot.getLong(FirestorePaths.USERS_CURRENT_STREAK)?.toInt()

            // Check if streak data changed
            val newCache = Pair(timestamp, streakCount)
            if (newCache == lastCachedData) return@addSnapshotListener
            lastCachedData = newCache

            if (timestamp == null || streakCount == null) {
                _currentStreakState.value = StreakState.NoStreak
                return@addSnapshotListener
            }

            val lastDate = timestamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val today = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(lastDate, today).toInt()

            val newState = when {
                daysBetween == 0 && streakCount == 1 -> StreakState.Started(streakCount)
                daysBetween == 0 && streakCount > 1 -> StreakState.Continued(streakCount)
                daysBetween == 1 -> StreakState.AtRisk(streakCount)
                daysBetween > 1 -> StreakState.Broken
                else -> StreakState.ErrorGettingData
            }

            _currentStreakState.value = newState
        }
    }

    override fun incrementAdWatchCount() {
        val userId = firebaseAuth.currentUser!!.uid
        val userDoc = firestore.collection(FirestorePaths.USERS).document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDoc)

            val currentStreak = snapshot.getLong(FirestorePaths.USERS_CURRENT_STREAK) ?: 0
            val priorTimestamp = snapshot.getTimestamp(FirestorePaths.USERS_LAST_WATCHED_TIMESTAMP)

            val today = LocalDate.now()
            val lastDate = priorTimestamp?.toDate()?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDate()

            val updates = mutableMapOf<String, Any>(
                FirestorePaths.USERS_TOTAL_ADS_WATCHED to FieldValue.increment(1)
            )

            // Only update streak & timestamp if it’s a new day
            if (lastDate == null || lastDate.isBefore(today)) {
                updates[FirestorePaths.USERS_CURRENT_STREAK] = currentStreak + 1
                updates[FirestorePaths.USERS_LAST_WATCHED_TIMESTAMP] = Timestamp.now()
            }

            transaction.set(userDoc, updates, SetOptions.merge())
        }.addOnFailureListener {
            Log.e("Firestore", "Failed to increment ad watch", it)
        }
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
                FirestorePaths.USERS_LAST_WATCHED_TIMESTAMP to FieldValue.delete(),
                FirestorePaths.USERS_CURRENT_STREAK to FieldValue.delete()
            )
        )
    }

}

