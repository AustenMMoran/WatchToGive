package com.ap.watchtogive.data.repository

import com.ap.watchtogive.model.Charity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CharitiesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : CharitiesRepository {
    override fun getCharitiesByLocation(location: String): Flow<List<Charity>> = callbackFlow {
        val collectionName = "charities_$location"

        val listenerRegistration: ListenerRegistration = firestore
            .collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close flow with error
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val charities = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Charity::class.java)
                    }
                    trySend(charities).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}
