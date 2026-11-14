package com.foodhealth.app.data

// =====================================================================
// FoodClass.kt : the 13 foods the ML model was trained on.
//
// !! ORDER MATTERS !! The entries below are in the exact order of the
// model's output (alphabetical, same as labels.txt). Output number 0 is
// BHAJI, number 12 is UGALI. Never reorder or insert in the middle.
// If the model is retrained with different foods, this file and
// labels.txt must change together (the classifier checks this on start).
// =====================================================================

enum class FoodClass(
    val label: String,                  // Exact text in labels.txt
    val displayName: String,            // What the user sees
    val category: MealCategory,         // reuses the Home screen chips
    val csvCode: Int?,                  // "Code" column of the CSV, null = placeholders
    val csvName: String?                // "Food Name" column of the CSV
) {
    BHAJI("bhaji", "Bhajia", MealCategory.CARBS, 15131, "Potato Bhajia"),
    CHAPATI("chapati", "Chapati", MealCategory.CARBS, 15019, "White Chapati"),
    GITHERI("githeri", "Githeri", MealCategory.COMBOS, 15053, "Githeri (stewed Maize & Beans)"),
    KACHUMBARI("kachumbari", "Kachumbari", MealCategory.VEGGIES, null, null),
    KUKUCHOMA("kukuchoma", "Kuku choma", MealCategory.PROTEINS, null, null),
    MANDAZI("mandazi", "Mandazi", MealCategory.CARBS, 15125, "Basic Mandazi (East African Doughnuts; Basic)"),
    MASALACHIPS("masalachips", "Masala chips", MealCategory.CARBS, 15110, "Potato Chips"),
    MATOKE("matoke", "Matoke", MealCategory.CARBS, 15056, "Matoke (Stewed Green Bananas & Meat)"),
    MUKIMO("mukimo", "Mukimo", MealCategory.COMBOS, 15054, "Mukimo (Maize, Beans, Potatoes & Pumpkin Leaves)"),
    NYAMACHOMA("nyamachoma", "Nyama choma", MealCategory.PROTEINS, null, null),
    PILAU("pilau", "Pilau", MealCategory.COMBOS, 15036, "Pilau (Spiced Rice)"),
    SUKUMAWIKI("sukumawiki", "Sukuma wiki", MealCategory.VEGGIES, 15031, "Sukumawiki (Stir fried kales)"),
    UGALI("ugali", "Ugali", MealCategory.CARBS, 15009, "Refined Maize Flour Ugali");

    val hasNutrientData: Boolean get() = csvCode !=null

    companion object {
        fun fromLabel(label: String): FoodClass? = entries.firstOrNull { it.label == label}
    }
}