
package com.foodhealth.app.data

import android.content.Context

// One nutrition record from the Kenyan Food Composition Table.
data class NutritionRecord(
    val classifierLabel: String,
    val code: Int,
    val foodName: String,
    val nutrients: Map<String, Double?>
) {
    // Retrieve a nutrient by its exact CSV column name.
    fun nutrient(name: String): Double? = nutrients[name]
}

// Reads nutrition records from the CSV stored in Android assets.
class NutritionRepository(context: Context) {

    private val records: List<NutritionRecord>

    init {
        records = context.applicationContext.assets
            .open(CSV_FILE)
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->

                val lines = reader.readLines()

                require(lines.isNotEmpty()) {
                    "Nutrition CSV is empty."
                }

                val headers = parseCsvLine(lines.first())
                    .map { it.removePrefix("\uFEFF").trim() }

                val labelIndex = headers.indexOf("Classifier_Label")
                val codeIndex = headers.indexOf("Code")
                val nameIndex = headers.indexOf("Food Name")

                require(
                    labelIndex >= 0 &&
                            codeIndex >= 0 &&
                            nameIndex >= 0
                ) {
                    "Required nutrition CSV columns are missing."
                }

                lines.drop(1).mapNotNull { line ->

                    if (line.isBlank()) return@mapNotNull null

                    val values = parseCsvLine(line)

                    if (values.size != headers.size) {
                        return@mapNotNull null
                    }

                    val code = values[codeIndex].toIntOrNull()
                        ?: return@mapNotNull null

                    val nutrients = headers.indices
                        .filter { it != labelIndex &&
                                it != codeIndex &&
                                it != nameIndex }
                        .associate { index ->
                            val value = values[index].trim()

                            val number = value.toDoubleOrNull()
                                ?.takeIf {
                                    !it.isNaN() && !it.isInfinite()
                                }

                            headers[index] to number
                        }

                    NutritionRecord(
                        classifierLabel = values[labelIndex].trim(),
                        code = code,
                        foodName = values[nameIndex].trim(),
                        nutrients = nutrients
                    )
                }
            }
    }

    // Get every nutrition record associated with a classifier label.
    fun recordsFor(food: FoodClass): List<NutritionRecord> {
        return records.filter {
            it.classifierLabel.equals(food.label, ignoreCase = true)
        }
    }

    // Get the specific record selected in FoodClass.kt.
    fun recordFor(food: FoodClass): NutritionRecord? {
        val selectedCode = food.csvCode ?: return null

        return records.firstOrNull {
            it.code == selectedCode &&
                    it.classifierLabel.equals(food.label, ignoreCase = true)
        }
    }

    // Get a record directly using its food composition table code.
    fun recordForCode(code: Int): NutritionRecord? {
        return records.firstOrNull { it.code == code }
    }

    // Number of successfully loaded nutrition records.
    fun recordCount(): Int = records.size

    companion object {
        private const val CSV_FILE = "food_nutrition_lookup.csv"

        // Handles commas inside quoted food names and escaped quotes.
        private fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            val field = StringBuilder()
            var insideQuotes = false
            var index = 0

            while (index < line.length) {
                val char = line[index]

                when {
                    char == '"' && insideQuotes &&
                            index + 1 < line.length &&
                            line[index + 1] == '"' -> {
                        field.append('"')
                        index++
                    }

                    char == '"' -> {
                        insideQuotes = !insideQuotes
                    }

                    char == ',' && !insideQuotes -> {
                        result.add(field.toString())
                        field.clear()
                    }

                    else -> {
                        field.append(char)
                    }
                }

                index++
            }

            result.add(field.toString())
            return result
        }
    }
}



