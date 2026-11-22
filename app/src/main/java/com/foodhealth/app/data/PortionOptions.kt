package com.foodhealth.app.data

// One choice in the portion dropdown, e.g. "1 serving" = 200 g.
data class PortionOption(val name: String, val grams: Int) {
    val display: String get() = "$name · $grams g"
}

// Rough, household-style portion sizes for each food the model can recognise.
// These are estimates; edit the numbers here and every screen updates.
object PortionOptions {

    fun forFood(food: FoodClass): List<PortionOption> = when (food) {
        FoodClass.UGALI -> listOf(
            PortionOption("Small (½ serving)", 100),
            PortionOption("1 serving", 200),
            PortionOption("1½ servings", 300),
            PortionOption("2 servings", 400)
        )
        FoodClass.CHAPATI -> listOf(
            PortionOption("1 chapati", 80),
            PortionOption("2 chapatis", 160),
            PortionOption("3 chapatis", 240),
            PortionOption("4 chapatis", 320)
        )
        FoodClass.MANDAZI -> listOf(
            PortionOption("1 mandazi", 50),
            PortionOption("2 mandazis", 100),
            PortionOption("3 mandazis", 150),
            PortionOption("4 mandazis", 200)
        )
        FoodClass.BHAJI -> listOf(
            PortionOption("½ serving (2-3 pieces)", 50),
            PortionOption("1 serving", 100),
            PortionOption("1½ servings", 150),
            PortionOption("2 servings", 200)
        )
        FoodClass.GITHERI -> listOf(
            PortionOption("½ serving", 125),
            PortionOption("1 serving", 250),
            PortionOption("1½ servings", 375),
            PortionOption("2 servings", 500)
        )
        FoodClass.MUKIMO -> listOf(
            PortionOption("½ serving", 125),
            PortionOption("1 serving", 250),
            PortionOption("1½ servings", 375),
            PortionOption("2 servings", 500)
        )
        FoodClass.PILAU -> listOf(
            PortionOption("½ serving", 125),
            PortionOption("1 serving", 250),
            PortionOption("1½ servings", 375),
            PortionOption("2 servings", 500)
        )
        FoodClass.MATOKE -> listOf(
            PortionOption("½ serving", 100),
            PortionOption("1 serving", 200),
            PortionOption("1½ servings", 300),
            PortionOption("2 servings", 400)
        )
        FoodClass.MASALACHIPS -> listOf(
            PortionOption("Small", 150),
            PortionOption("Regular", 250),
            PortionOption("Large", 350)
        )
        FoodClass.SUKUMAWIKI -> listOf(
            PortionOption("Small side", 60),
            PortionOption("1 serving", 100),
            PortionOption("2 servings", 200)
        )
        FoodClass.KACHUMBARI -> listOf(
            PortionOption("Small spoonful", 40),
            PortionOption("1 side serving", 80),
            PortionOption("2 servings", 160)
        )
        FoodClass.KUKUCHOMA -> listOf(
            PortionOption("1 piece (leg or thigh)", 100),
            PortionOption("Quarter chicken", 200),
            PortionOption("Half chicken", 400)
        )
        FoodClass.NYAMACHOMA -> listOf(
            PortionOption("Small", 100),
            PortionOption("1 serving", 150),
            PortionOption("¼ kg", 250),
            PortionOption("½ kg", 500)
        )
    }
}