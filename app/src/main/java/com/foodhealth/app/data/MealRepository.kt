package com.foodhealth.app.data

import com.foodhealth.app.ml.FoodPrediction
import com.foodhealth.app.util.DateKeys
import com.foodhealth.app.util.awaitResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// =====================================================================
// MealRepository.kt : the ONLY place that talks to Firestore about meals.
//
//   users/{uid}/meals/{mealId}           saved when the user taps "Proceed"
//   users/{uid}/analyzed_meals/{mealId}  saved when the analysis is ready
//
// No photos are stored (free plan, no Firebase Storage).
// All functions are "suspend": call them inside a coroutine.
// =====================================================================
class MealRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun currentUserDoc(): DocumentReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("No signed-in user")
        return db.collection(USERS).document(uid)
    }

    // Saves the scanned meal. The mealId is made once per scan on the phone,
    // so tapping "Proceed" twice overwrites the same document (no duplicates).
    suspend fun saveScannedMeal(
        mealId: String,
        source: ScanSource,
        predictions: List<FoodPrediction>,
        confirmed: List<FoodClass>,
        edited: Boolean
    ) {
        val user = currentUserDoc()
        val now = Timestamp.now()
        val meal = hashMapOf<String, Any>(
            "mealId" to mealId,
            "foods" to confirmed.map { mapOf("label" to it.label, "name" to it.displayName) },
            "predictions" to predictions.take(3).map {
                mapOf(
                    "label" to it.food.label,
                    "name" to it.food.displayName,
                    "probability" to it.probability.toDouble()
                )
            },
            "edited" to edited,
            "source" to source.value,
            "dateKey" to DateKeys.keyFor(now.toDate().time),
            "createdAt" to now,
            "rating" to 0,
            "analyzed" to false
        )
        // One batch = both writes succeed together or not at all.
        db.batch()
            .set(user, mapOf("email" to (auth.currentUser?.email ?: "")), SetOptions.merge())
            .set(user.collection(MEALS).document(mealId), meal)
            .commit()
            .awaitResult()
    }

    // For the nutrient teammate: call this when the analysis is ready.
    suspend fun saveAnalysis(mealId: String, analysis: MealAnalysis) {
        val user = currentUserDoc()
        val data = hashMapOf<String, Any?>(
            "mealId" to mealId,
            "analyzedAt" to Timestamp.now(),
            "calories" to analysis.calories,
            "proteinG" to analysis.proteinG,
            "carbsG" to analysis.carbsG,
            "fatG" to analysis.fatG,
            "fibreG" to analysis.fibreG,
            "vitamins" to analysis.vitamins,
            "minerals" to analysis.minerals,
            "balanceScore" to analysis.balanceScore,
            "advice" to analysis.advice,
            "placeholderFoods" to analysis.placeholderFoods
        )
        db.batch()
            .set(user.collection(ANALYZED).document(mealId), data)
            .update(user.collection(MEALS).document(mealId), "analyzed", true)
            .commit()
            .awaitResult()
    }

    suspend fun saveRating(mealId: String, rating: Int) {
        currentUserDoc().collection(MEALS).document(mealId)
            .update("rating", rating)
            .awaitResult()
    }

    // All meals of one day, oldest first. dateKey looks like "2026-11-14".
    suspend fun mealsForDay(dateKey: String): List<StoredMeal> {
        val snapshot = currentUserDoc().collection(MEALS)
            .whereEqualTo("dateKey", dateKey)
            .get()
            .awaitResult()

        return snapshot.documents.map { doc ->
            val names = (doc.get("foods") as? List<*>)
                ?.mapNotNull { (it as? Map<*, *>)?.get("name") as? String }
                ?: emptyList()
            StoredMeal(
                id = doc.id,
                foodNames = names,
                createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                rating = (doc.getLong("rating") ?: 0L).toInt(),
                analyzed = doc.getBoolean("analyzed") ?: false,
                source = doc.getString("source") ?: ""
            )
        }.sortedBy { it.createdAtMillis }
    }

    private companion object {
        const val USERS = "users"
        const val MEALS = "meals"
        const val ANALYZED = "analyzed_meals"
    }
}