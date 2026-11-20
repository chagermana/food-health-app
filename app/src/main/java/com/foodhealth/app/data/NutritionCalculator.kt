
package com.foodhealth.app.data

// The amount of one food selected by the user.
data class FoodPortion(
    val food: FoodClass,
    val grams: Double
)

// Calculates the total nutrients for a meal.
class NutritionCalculator(
    private val repository: NutritionRepository
) {

    fun calculate(portions: List<FoodPortion>): MealAnalysis {

        require(portions.isNotEmpty()) {
            "At least one food portion is required."
        }

        require(portions.all {
            it.grams > 0.0 && it.grams.isFinite()
        }) {
            "Every food portion must have a positive, valid weight."
        }

        // Retrieve the matching nutrition record for each food.
        val availableRecords = portions.mapNotNull { portion ->

            val record = repository.recordFor(portion.food)

            if (record == null) {
                null
            } else {
                Triple(portion.food, portion.grams, record)
            }
        }

        // Foods without matching nutrition records.
        val placeholders = portions
            .filter { repository.recordFor(it.food) == null }
            .map { it.food.label }

        // Convert a per-100g value into the value for the user's portion.
        fun calculateNutrient(column: String): Double? {
            if (availableRecords.isEmpty()) return null

            val values = availableRecords.map { (_, grams, record) ->
                val value = record.nutrient(column) ?: return null
                value * grams / 100.0
            }

            return values.sum()
        }

        // Collect available vitamin values.
        val vitamins = listOf(
            "VitA_RAE",
            "Thiamin",
            "Riboflavin",
            "Niacin",
            "VitB12",
            "VitC"
        ).mapNotNull { nutrient ->

            calculateNutrient(nutrient)?.let {
                nutrient to it
            }
        }.toMap()

        // Collect available mineral values.
        val minerals = listOf(
            "Calcium (mg)",
            "Iron (mg)",
            "Magnesium (mg)",
            "Phosphorus (mg)",
            "Potassium (mg)",
            "Sodium (mg)",
            "Zinc (mg)",
            "Selenium (mcg)"
        ).mapNotNull { nutrient ->

            calculateNutrient(nutrient)?.let {
                nutrient to it
            }
        }.toMap()

        val calories = calculateNutrient("Energy_(kcal)")
        val protein = calculateNutrient("Protein (g)")
        val carbs = calculateNutrient("Carb (g)")
        val fat = calculateNutrient("Fat (g)")
        val fibre = calculateNutrient("Fibre (g)")

        val balanceScore = MealBalanceScorer.calculate(
            calories = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            fibreG = fibre
        )

        val result = MealAnalysis(
            calories = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            fibreG = fibre,
            vitamins = vitamins,
            minerals = minerals,
            balanceScore = balanceScore,
            advice = null,
            placeholderFoods = placeholders
        )

        return result.copy(
            advice = MealAdviceGenerator.generate(result)
        )
    }
}
