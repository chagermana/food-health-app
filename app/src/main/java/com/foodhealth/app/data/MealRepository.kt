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
//   users/{uid}/meals/{mealId}           saved when the user taps "Proceed";
//                                        updated with the nutrient totals and
//                                        portion grams when the analysis is saved
//   users/{uid}/analyzed_meals/{mealId}  full analysis (vitamins, minerals, advice)
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
        db.batch()
            .set(user, mapOf("email" to (auth.currentUser?.email ?: "")), SetOptions.merge())
            .set(user.collection(MEALS).document(mealId), meal)
            .commit()
            .awaitResult()
    }

    // portionGrams = label -> grams the user picked, e.g. "ugali" to 200.
    suspend fun saveAnalysis(
        mealId: String,
        analysis: MealAnalysis,
        portionGrams: Map<String, Int> = emptyMap()
    ) {
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
        // The meal document also keeps the totals, so the Profile page can read
        // a whole week with ONE query on "meals".
        val mealFields = hashMapOf<String, Any?>(
            "analyzed" to true,
            "calories" to analysis.calories,
            "proteinG" to analysis.proteinG,
            "carbsG" to analysis.carbsG,
            "fatG" to analysis.fatG,
            "fibreG" to analysis.fibreG,
            "balanceScore" to analysis.balanceScore,
            "portions" to portionGrams
        )
        db.batch()
            .set(user.collection(ANALYZED).document(mealId), data)
            .update(user.collection(MEALS).document(mealId), mealFields)
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

    // Every meal between two dateKeys (inclusive) with its stored totals.
    // Range on ONE field, so no Firestore index is needed.
    suspend fun mealsBetween(startKey: String, endKey: String): List<MealRecord> {
        val snapshot = currentUserDoc().collection(MEALS)
            .whereGreaterThanOrEqualTo("dateKey", startKey)
            .whereLessThanOrEqualTo("dateKey", endKey)
            .get()
            .awaitResult()

        return snapshot.documents.map { doc ->
            val labels = (doc.get("foods") as? List<*>)
                ?.mapNotNull { (it as? Map<*, *>)?.get("label") as? String }
                ?: emptyList()
            val portions = (doc.get("portions") as? Map<*, *>)
                ?.mapNotNull { (k, v) ->
                    val key = k as? String
                    val grams = (v as? Number)?.toDouble()
                    if (key != null && grams != null) key to grams else null
                }
                ?.toMap()
                ?: emptyMap()
            MealRecord(
                dateKey = doc.getString("dateKey") ?: "",
                createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                foodLabels = labels,
                calories = doc.getDouble("calories") ?: 0.0,
                proteinG = doc.getDouble("proteinG") ?: 0.0,
                carbsG = doc.getDouble("carbsG") ?: 0.0,
                fatG = doc.getDouble("fatG") ?: 0.0,
                portionGrams = portions
            )
        }
    }

    private companion object {
        const val USERS = "users"
        const val MEALS = "meals"
        const val ANALYZED = "analyzed_meals"
    }
}