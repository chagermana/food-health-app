package com.foodhealth.app.data

import android.app.appsearch.StorageInfo
import androidx.annotation.DrawableRes
import com.foodhealth.app.R
import kotlin.math.roundToInt

// One meal the user may pick or scan.
data class Meal(
    val id: String,                 //must match a line labels.txt of the ML model later
    val name: String,               // text shown to the user
    @DrawableRes val imageRes: Int, // photo in res/drawable
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fibreG: Int
)

// 4 placeholder meals from the prototype.
object SampleMeals{
    val all = listOf(
        Meal("plate", "Ugali, skuma & stew", R.drawable.meal_plate, 680, 28, 82,24,10),
        Meal("ugali", "Ugali", R.drawable.meal_ugali, 330, 7, 72, 1, 3),
        Meal("chapati", "Chapati", R.drawable.meal_chapati, 300, 7, 46, 9, 3),
        Meal("mandazi", "Mandazi", R.drawable.meal_mandazi, 270, 5, 36, 11, 1)
    )

    // Find a meal from the id the ML model returns, or null if unknown
    fun byId(id: String): Meal? = all.firstOrNull() {it.id == id }
}

// Questions asked on the portions and meal prep screen
enum class PortionSize(val label: String, val factor: Float){
    SMALL("Small", 0.8f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.25f)
}

enum class CookedMethod(val label: String, val kcalFactor: Float) {
    BOILED("Boiled", 1.0f),
    FRIED("Fried", 1.2f),
    GRILLED("Grilled", 1.0f)
}

// Placeholder - Extra calories
enum class Extra(val label: String, val extraKcal: Int){
    OIL("Cooking oil", 90),
    SUGAR_TEA("Sugar tea", 60),
    AVOCADO("Avocado", 120)
}

// Daily Targets used by the analysis screen
object NutritionTargets {
    const val MEAL_KCAL = 650
    const val PROTEIN_G = 35
    const val CARBS_G = 130
    const val FAT_G = 35
    const val FIBRE_G = 25
}

// Static Data example of what the screen shows
data class NutritionResult(
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fibreG: Int,
    val advice: String
)

// The nutrient calculation: meal x portion size x cooking method + extras.
// Real version (week 4): per-100g values from the CSV x grams of the portion.
// Keep this as the ONE place where the maths happens.
fun calculateNutrition(
    meal: Meal,
    size: PortionSize,
    cook: CookedMethod,
    extras: Set<Extra>
): NutritionResult {
    val kcal = (meal.calories * size.factor * cook.kcalFactor).roundToInt() +
            extras.sumOf { it.extraKcal }
    val protein = (meal.proteinG * size.factor).roundToInt()
    val carbs = (meal.carbsG * size.factor).roundToInt()
    val fat = (meal.fatG * size.factor).roundToInt()
    val fibre = (meal.fibreG * size.factor).roundToInt()

    // Advice is a simple if/else rule, as decided in the project plan.
    val advice = if (protein < 20) {
        "Plenty of energy from carbs, but light on protein. " +
                "Adding beans, eggs or fish would balance it."
    } else {
        "A solid, protein-rich meal. Add more vegetables to lift your fibre."
    }
    return NutritionResult(kcal, protein, carbs, fat, fibre, advice)
}

