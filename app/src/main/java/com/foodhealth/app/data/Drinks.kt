package com.foodhealth.app.data

// Drinks are logged separately from meals. They never change a meal's nutrition.
data class Drink(
    val id: String,
    val name: String,
    val imageName: String,          // file name in res/drawable
    val isWater: Boolean = false,
    val isFruitJuice: Boolean = false
)

// One saved drink entry read back from Firestore (users/{uid}/drinks).
data class DrinkLog(
    val drinkId: String,
    val cups: Int,
    val dateKey: String,
    val createdAtMillis: Long
)

object Drinks {
    const val CUP_ML = 250                 // 1 glass
    const val JUICE_FRUIT_G_PER_CUP = 80   // counts as one fruit portion per glass

    val all = listOf(
        Drink("water", "Water", "drink_water", isWater = true),
        Drink("orange_juice", "Orange juice", "drink_orange_juice", isFruitJuice = true),
        Drink("mango_juice", "Mango juice", "drink_mango_juice", isFruitJuice = true),
        Drink("passion_juice", "Passion juice", "drink_passion_juice", isFruitJuice = true),
        Drink("tea_milk", "Tea with milk", "drink_tea_milk")
    )

    fun byId(id: String): Drink? = all.firstOrNull { it.id == id }
}