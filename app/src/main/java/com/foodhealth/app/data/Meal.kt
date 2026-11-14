package com.foodhealth.app.data

import androidx.annotation.DrawableRes
import com.foodhealth.app.R

// Placeholder meals for the Home grid, until real food data is added.
// Categories shown as the small menu chips on the Home screen.
enum class MealCategory(val label: String) {
    CARBS("Carbs"),
    PROTEINS("Proteins"),
    VEGGIES("Veggies"),
    FRUITS("Fruits"),
    COMBOS("Common combos"),
    DRINKS("Drinks")
}

// One preset meal the user may tap on Home (manual fallback to scanning).
data class Meal(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fibreG: Int,
    val category: MealCategory = MealCategory.COMBOS
)

object SampleMeals {
    val all = listOf(
        Meal("plate", "Ugali, skuma & stew", R.drawable.meal_plate, 680, 28, 82, 24, 10, MealCategory.COMBOS),
        Meal("ugali", "Ugali", R.drawable.meal_ugali, 330, 7, 72, 1, 3, MealCategory.CARBS),
        Meal("chapati", "Chapati", R.drawable.meal_chapati, 300, 7, 46, 9, 3, MealCategory.CARBS),
        Meal("mandazi", "Mandazi", R.drawable.meal_mandazi, 270, 5, 36, 11, 1, MealCategory.CARBS)
    )

    fun byId(id: String): Meal? = all.firstOrNull { it.id == id }
}

// Per-meal targets (kept for the nutrient-analysis teammate to use).
object NutritionTargets {
    const val MEAL_KCAL = 650
    const val PROTEIN_G = 35
    const val CARBS_G = 130
    const val FAT_G = 35
    const val FIBRE_G = 25
}