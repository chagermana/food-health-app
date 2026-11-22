package com.foodhealth.app.data

import com.foodhealth.app.util.DateKeys
import com.foodhealth.app.util.awaitResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

// users/{uid}/drinks/{autoId} : one document per "I had N glasses of X".
class DrinkRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun currentUserDoc(): DocumentReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("No signed-in user")
        return db.collection("users").document(uid)
    }

    suspend fun logDrink(drinkId: String, cups: Int) {
        val now = Timestamp.now()
        val data = hashMapOf<String, Any>(
            "drinkId" to drinkId,
            "cups" to cups,
            "ml" to cups * Drinks.CUP_ML,
            "dateKey" to DateKeys.keyFor(now.toDate().time),
            "createdAt" to now
        )
        currentUserDoc().collection(DRINKS).add(data).awaitResult()
    }

    // All drinks between two dateKeys (inclusive), e.g. "2026-11-16" .. "2026-11-22".
    suspend fun drinksBetween(startKey: String, endKey: String): List<DrinkLog> {
        val snapshot = currentUserDoc().collection(DRINKS)
            .whereGreaterThanOrEqualTo("dateKey", startKey)
            .whereLessThanOrEqualTo("dateKey", endKey)
            .get()
            .awaitResult()

        return snapshot.documents.map { doc ->
            DrinkLog(
                drinkId = doc.getString("drinkId") ?: "",
                cups = (doc.getLong("cups") ?: 0L).toInt(),
                dateKey = doc.getString("dateKey") ?: "",
                createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
            )
        }
    }

    private companion object {
        const val DRINKS = "drinks"
    }
}