package com.foodhealth.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.foodhealth.app.data.CookedMethod
import com.foodhealth.app.data.Extra
import com.foodhealth.app.data.Meal
import com.foodhealth.app.data.NutritionResult
import com.foodhealth.app.data.PortionSize
import com.foodhealth.app.data.calculateNutrition

// =====================================================================
// MealFlowState.kt : remembers the meal the user is working on while they
// move through Home -> Portion -> Analysis -> Rate.
// =====================================================================
class MealFlowState {
    var meal by mutableStateOf<Meal?>(null)
        private set                       // only startMeal() may change it
    var size by mutableStateOf(PortionSize.MEDIUM)
    var cook by mutableStateOf(CookedMethod.BOILED)
    var extras by mutableStateOf<Set<Extra>>(emptySet())
    var rating by mutableIntStateOf(0)    // 0 = not rated, 1..5 stars

    // Called when the user taps a meal OR the scan recognises one.
    fun startMeal(newMeal: Meal) {
        meal = newMeal
        size = PortionSize.MEDIUM
        cook = CookedMethod.BOILED
        extras = emptySet()
        rating = 0
    }

    // The numbers shown on the analysis screen, worked out from the choices.
    val result: NutritionResult?
        get() = meal?.let { calculateNutrition(it, size, cook, extras) }
}
