package com.foodhealth.app.data

import kotlin.math.abs
import kotlin.math.roundToInt

object MealBalanceScorer {

    /**
     * Calculates an experimental meal-balance score from 0 to 10.
     * Each of the five nutrients contributes a maximum of 2 points.
     */

    fun calculate(
        calories: Double?,
        proteinG: Double?,
        carbsG: Double?,
        fatG: Double?,
        fibreG: Double?
    ): Int? {

        // A complete score requires all five nutrient values.
        if (
            calories == null ||
            proteinG == null ||
            carbsG == null ||
            fatG == null ||
            fibreG == null
        ) {
            return null
        }

        val values = listOf(
            calories,
            proteinG,
            carbsG,
            fatG,
            fibreG
        )

        if (values.any { !it.isFinite() || it < 0.0 }) {
            return null
        }

        // Each nutrient contributes up to 2 points.
        fun nutrientPoints(actual: Double, target: Double): Double {
            val difference = abs(actual - target) / target

            return (2.0 * (1.0 - difference))
                .coerceIn(0.0, 2.0)
        }

        val totalScore =
            nutrientPoints(calories, NutritionTargets.MEAL_KCAL.toDouble()) +
                    nutrientPoints(proteinG, NutritionTargets.PROTEIN_G.toDouble()) +
                    nutrientPoints(carbsG, NutritionTargets.CARBS_G.toDouble()) +
                    nutrientPoints(fatG, NutritionTargets.FAT_G.toDouble()) +
                    nutrientPoints(fibreG, NutritionTargets.FIBRE_G.toDouble())

        return totalScore.roundToInt().coerceIn(0, 10)
    }
}