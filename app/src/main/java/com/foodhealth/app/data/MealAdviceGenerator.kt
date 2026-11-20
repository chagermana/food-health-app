package com.foodhealth.app.data

object MealAdviceGenerator {

    fun generate(analysis: MealAnalysis): String {

        val feedback = mutableListOf<String>()

        // Explain when some selected foods have no matching
        // nutrition record in the dataset.
        if (analysis.placeholderFoods.isNotEmpty()) {
            feedback.add(
                "Nutrition data is unavailable for: " +
                        analysis.placeholderFoods.joinToString(", ") +
                        ". The displayed nutrient totals exclude these foods."
            )
        }

        // Describe the available nutrient information.
        if (analysis.calories != null) {
            feedback.add(
                "Your meal's estimated energy is " +
                        "${"%.1f".format(analysis.calories)} kcal."
            )
        }

        if (analysis.proteinG != null) {
            feedback.add(
                "The meal provides approximately " +
                        "${"%.1f".format(analysis.proteinG)} g of protein."
            )
        }

        if (analysis.fibreG != null) {
            feedback.add(
                "The meal provides approximately " +
                        "${"%.1f".format(analysis.fibreG)} g of dietary fibre."
            )
        }

        if (feedback.isEmpty()) {
            return "There is not enough nutrition data to generate a summary."
        }

        return feedback.joinToString("\n\n")
    }
}