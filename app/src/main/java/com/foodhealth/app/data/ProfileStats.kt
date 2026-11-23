package com.foodhealth.app.data

// One saved meal, read back with the numbers stored by saveAnalysis().
data class MealRecord(
    val dateKey: String,
    val createdAtMillis: Long,
    val foodLabels: List<String>,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val portionGrams: Map<String, Double>   // label -> grams eaten
) {
    // Only Kachumbari and Sukuma wiki count (MealCategory.VEGGIES).
    val fruitVegG: Double
        get() = portionGrams
            .filterKeys { FoodClass.fromLabel(it)?.category == MealCategory.VEGGIES }
            .values.sum()
}

data class DayTotals(
    val dateKey: String,
    val meals: Int,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fruitVegG: Double,
    val waterL: Double
)

data class ProfileStats(
    val today: DayTotals,
    val week: List<DayTotals>,                    // last 7 days, oldest first
    val macroSplit: List<Float>,                  // protein, carbs, fat shares (0 if no data)
    val streak: Int,
    val topFoods: List<Pair<FoodClass, Int>>      // best first, max 3
)

object ProfileStatsBuilder {

    // keys = dateKeys oldest first, ending with today (at least 7 of them).
    fun build(meals: List<MealRecord>, drinks: List<DrinkLog>, keys: List<String>): ProfileStats {
        val mealsByDay = meals.groupBy { it.dateKey }
        val drinksByDay = drinks.groupBy { it.dateKey }

        fun totals(key: String): DayTotals {
            val m = mealsByDay[key].orEmpty()
            val d = drinksByDay[key].orEmpty()
            val waterCups = d.filter { Drinks.byId(it.drinkId)?.isWater == true }.sumOf { it.cups }
            val juiceCups = d.filter { Drinks.byId(it.drinkId)?.isFruitJuice == true }.sumOf { it.cups }
            return DayTotals(
                dateKey = key,
                meals = m.size,
                kcal = m.sumOf { it.calories },
                proteinG = m.sumOf { it.proteinG },
                carbsG = m.sumOf { it.carbsG },
                fatG = m.sumOf { it.fatG },
                fruitVegG = m.sumOf { it.fruitVegG } + juiceCups * Drinks.JUICE_FRUIT_G_PER_CUP,
                waterL = waterCups * Drinks.CUP_ML / 1000.0
            )
        }

        val week = keys.takeLast(7).map { totals(it) }

        // Average macro split of the week, as each macro's share of energy.
        val pE = week.sumOf { it.proteinG } * 4
        val cE = week.sumOf { it.carbsG } * 4
        val fE = week.sumOf { it.fatG } * 9
        val total = pE + cE + fE
        val split = if (total > 0) {
            listOf((pE / total).toFloat(), (cE / total).toFloat(), (fE / total).toFloat())
        } else listOf(0f, 0f, 0f)

        // Streak: consecutive days with a logged meal, counting back from today.
        // If nothing is logged yet today, the streak is not broken until the day ends.
        val daysWithMeals = mealsByDay.keys
        var i = keys.lastIndex
        if (i >= 0 && keys[i] !in daysWithMeals) i--
        var streak = 0
        while (i >= 0 && keys[i] in daysWithMeals) {
            streak++
            i--
        }

        val top = meals.flatMap { it.foodLabels }
            .groupingBy { it }.eachCount().entries
            .mapNotNull { (label, n) -> FoodClass.fromLabel(label)?.let { it to n } }
            .sortedWith(compareByDescending<Pair<FoodClass, Int>> { it.second }.thenBy { it.first.label })
            .take(3)

        return ProfileStats(totals(keys.last()), week, split, streak, top)
    }
}