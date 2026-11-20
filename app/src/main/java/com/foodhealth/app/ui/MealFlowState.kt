package com.foodhealth.app.ui

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.foodhealth.app.data.FoodClass
import com.foodhealth.app.data.Meal
import com.foodhealth.app.data.ScanSource
import com.foodhealth.app.ml.FoodPrediction
import java.util.UUID
import androidx.compose.runtime.mutableStateMapOf
import com.foodhealth.app.data.MealAnalysis
// =====================================================================
// MealFlowState.kt : remembers the meal the user is working on while they
// move through Home -> Scan result -> Analysis -> Rate.
//   photo / predictions : what the camera/gallery + the ML model gave us
//   selected            : the foods the USER confirmed (1 to 3), editable
// =====================================================================
class MealFlowState {
    var photo by mutableStateOf<Bitmap?>(null)
        private set
    var presetImageRes by mutableStateOf<Int?>(null)    // only for manual preset meals
        private set
    var source by mutableStateOf(ScanSource.GALLERY)
        private set
    var predictions by mutableStateOf<List<FoodPrediction>>(emptyList())   // all 13, best first
        private set
    var selected by mutableStateOf<List<FoodClass>>(emptyList())
        private set
    var mealId by mutableStateOf("")                    // same id in Firestore
        private set
    var mealSaved by mutableStateOf(false)              // true once "Proceed" saved it
        private set
    var rating by mutableIntStateOf(0)

    // 0 = not rated, 1..5 stars
    var analysis by mutableStateOf<MealAnalysis?>(null)
        private set

    fun updateAnalysis(value: MealAnalysis) {
        analysis = value
    }

    private var suggested: List<FoodClass> = emptyList()   // what the model suggested

    val hasMeal: Boolean get() = selected.isNotEmpty()
    val readyForAnalysis: Boolean get() = hasMeal && mealSaved
    val wasEdited: Boolean get() = selected != suggested

    // Stores the portion sizes entered for each confirmed food.
    val portionInputs = mutableStateMapOf<String, String>()


    // Called after the ML model has looked at a photo.
    fun startScan(newPhoto: Bitmap, newSource: ScanSource, newPredictions: List<FoodPrediction>) {
        photo = newPhoto
        presetImageRes = null
        source = newSource
        predictions = newPredictions
        suggested = defaultSelection(newPredictions)
        selected = suggested
        newMeal()
    }

    // Called when the user taps a preset meal on Home (no photo, no percentages).
    fun startManual(meal: Meal) {
        photo = null
        presetImageRes = meal.imageRes
        source = ScanSource.MANUAL
        predictions = emptyList()
        suggested = when (meal.id) {
            "plate" -> listOf(FoodClass.UGALI, FoodClass.SUKUMAWIKI)   // "Ugali, skuma & stew"
            else -> listOfNotNull(FoodClass.fromLabel(meal.id))
        }
        selected = suggested
        newMeal()
    }

    private fun newMeal() {
        mealId = UUID.randomUUID().toString()
        mealSaved = false
        rating = 0
        analysis = null

        // Clear portions from the previous meal.
        portionInputs.clear()

        // Initialise empty portion inputs for the new meal.
        selected.forEach { food ->
            portionInputs[food.label] = ""
        }
    }

    // The model's confidence for one food (0f..1f); 0 if there was no scan.
    fun probabilityOf(food: FoodClass): Float =
        predictions.firstOrNull { it.food == food }?.probability ?: 0f

    // Foods the dropdown of row [index] may offer: its own food plus every
    // food not already on the plate. Most likely first.
    fun optionsFor(index: Int): List<FoodClass> =
        FoodClass.entries
            .filter { it == selected[index] || it !in selected }
            .sortedByDescending { probabilityOf(it) }

    fun changeFood(index: Int, food: FoodClass) {
        if (index !in selected.indices) return
        if (food in selected && selected[index] != food) return

        val oldFood = selected[index]

        selected = selected.toMutableList().also {
            it[index] = food
        }

        // Remove the old food's portion.
        portionInputs.remove(oldFood.label)

        // Initialise the new food's portion.
        portionInputs[food.label] = ""

        mealSaved = false
    }

    fun removeFood(index: Int) {
        if (selected.size <= 1) return

        val removedFood = selected[index]

        selected = selected.filterIndexed { i, _ ->
            i != index
        }

        // Remove the deleted food's portion.
        portionInputs.remove(removedFood.label)

        mealSaved = false
    }

    fun addFood() {
        if (selected.size >= MAX_FOODS) return
        val next = FoodClass.entries
            .filter { it !in selected }
            .maxByOrNull { probabilityOf(it) } ?: return
        selected = selected + next

        // Initialise a portion input for the added food.
        portionInputs[next.label] = ""

        mealSaved = false
    }

    fun markSaved() {
        mealSaved = true
    }

    // Best guess always, plus the next ones if the model is at least 15% sure.
    private fun defaultSelection(p: List<FoodPrediction>): List<FoodClass> =
        p.take(MAX_FOODS)
            .filterIndexed { i, pred -> i == 0 || pred.probability >= PLATE_MIN_PROBABILITY }
            .map { it.food }

    companion object {
        const val MAX_FOODS = 3
        const val PLATE_MIN_PROBABILITY = 0.15f
    }
}