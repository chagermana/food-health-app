package com.foodhealth.app.data

// Where the meal photo came from. MANUAL = user tapped a preset meal on Home.
enum class ScanSource(val value: String) {
    CAMERA("camera"),
    GALLERY("gallery"),
    MANUAL("manual")
}

// One meal read back from Firestore (users/{uid}/meals) for the diary.
data class StoredMeal(
    val id: String,
    val foodNames: List<String>,
    val createdAtMillis: Long,
    val rating: Int,
    val analyzed: Boolean,
    val source: String
)

// What the nutrient analysis produces. Saved by MealRepository.saveAnalysis()
// into users/{uid}/analyzed_meals/{mealId}. Filled in by the nutrient teammate;
// every field is optional so partial results are fine.
data class MealAnalysis(
    val calories: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val fibreG: Double? = null,
    val vitamins: Map<String, Double> = emptyMap(),   // e.g. "VitC" to 12.0
    val minerals: Map<String, Double> = emptyMap(),   // e.g. "Iron" to 4.5
    val balanceScore: Int? = null,                    // 0..10, shown on the Rate screen
    val advice: String? = null,
    val placeholderFoods: List<String> = emptyList()  // labels that used placeholder data
)