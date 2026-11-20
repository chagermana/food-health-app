
package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.foodhealth.app.data.FoodPortion
import com.foodhealth.app.data.NutritionCalculator
import com.foodhealth.app.data.NutritionRepository

import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.FlowPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard

import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.TextDark

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import com.foodhealth.app.data.MealRepository
import kotlinx.coroutines.launch


@Composable
fun AnalysisScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {

    // Check whether a confirmed meal is available.
    if (!flow.readyForAnalysis) {
        EmptyState(
            Icons.Outlined.Analytics,
            "Nothing to analyse yet",
            "Scan or upload a meal and confirm the foods. The analysis will show here.",
            "Choose a meal",
            onGoHome
        )
        return
    }

    // ---------------------------------------------------------
    // STEP 1: INITIALISE THE NUTRITION REPOSITORY
    // ---------------------------------------------------------

    val context = LocalContext.current

    // Load the nutrition CSV from the app's assets folder.
    val nutritionRepository = remember(context) {
        NutritionRepository(context)
    }

    // Create the calculator that uses the repository.
    val nutritionCalculator = remember(nutritionRepository) {
        NutritionCalculator(nutritionRepository)
    }

    // ---------------------------------------------------------
    // STEP 2: STORE THE PORTION SIZES ENTERED BY THE USER
    // ---------------------------------------------------------

    val portionInputs = flow.portionInputs

    // ---------------------------------------------------------
    // STEP 3: CONVERT THE INPUTS INTO FOOD PORTIONS
    // ---------------------------------------------------------

    val portions = flow.selected.mapNotNull { food ->

        val grams = portionInputs[food.label]?.toDoubleOrNull()

        if (grams != null && grams > 0) {
            FoodPortion(
                food = food,
                grams = grams
            )
        } else {
            null
        }
    }

    // Check whether every selected food has a valid portion.
    val allPortionsEntered =
        portions.size == flow.selected.size &&
                portions.isNotEmpty()

    // ---------------------------------------------------------
    // STEP 4: CALCULATE THE NUTRITION VALUES
    // ---------------------------------------------------------

    val analysis = remember(
        portionInputs.toMap(),
        flow.selected
    ) {
        if (allPortionsEntered) {
            nutritionCalculator.calculate(portions)
        } else {
            null
        }
    }
    // ---------------------------------------------------------
// STEP 5: PREPARE FIRESTORE SAVE
// ---------------------------------------------------------

    val coroutineScope = rememberCoroutineScope()

    val isSaving = remember(flow.mealId) {
        mutableStateOf(false)
    }

    val saveError = remember(flow.mealId) {
        mutableStateOf<String?>(null)
    }
    // ---------------------------------------------------------
    // STEP 5: DISPLAY THE ANALYSIS SCREEN
    // ---------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Peach, Cream, Cream)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {

        // Top navigation bar.
        ScreenTopBar(
            title = "Meal analysis",
            onBack = onBack,
            infoText = "Your calories, macros, vitamins and minerals for this meal will appear here."
        )

        // Display the scanned meal photo.
        FlowPhotoCard(
            flow,
            Modifier.fillMaxWidth(),
            height = 140.dp
        )

        // -----------------------------------------------------
        // CONFIRMED FOODS
        // -----------------------------------------------------

        SectionCard(
            Modifier.padding(top = 14.dp)
        ) {

            Text(
                "Foods in this meal",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            flow.selected.forEach { food ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        food.displayName,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        color = TextDark
                    )

                    Text(
                        if (food.hasNutrientData) {
                            "Nutrient data ready"
                        } else {
                            "Placeholder data"
                        },
                        fontSize = 11.sp,
                        color = if (food.hasNutrientData) {
                            FoodGreen
                        } else {
                            FoodOrange
                        }
                    )
                }
            }
        }

        // -----------------------------------------------------
        // PORTION SIZE INPUTS
        // -----------------------------------------------------

        SectionCard(
            Modifier.padding(top = 14.dp)
        ) {

            Text(
                "Portion sizes",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Text(
                "Enter approximately how many grams of each food you ate.",
                fontSize = 12.sp,
                color = Muted,
                modifier = Modifier.padding(
                    top = 6.dp,
                    bottom = 12.dp
                )
            )

            flow.selected.forEach { food ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            food.displayName,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            fontSize = 14.sp
                        )

                        Text(
                            "Enter weight in grams",
                            fontSize = 11.sp,
                            color = Muted
                        )
                    }

                    OutlinedTextField(
                        value = portionInputs[food.label] ?: "",

                        onValueChange = { newValue ->
                            portionInputs[food.label] =
                                newValue
                                    .filter { it.isDigit() }
                                    .take(5)
                        },

                        label = {
                            Text("Grams")
                        },

                        suffix = {
                            Text("g")
                        },

                        singleLine = true,

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),

                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }

        // -----------------------------------------------------
        // CALORIES AND MACRONUTRIENTS
        // -----------------------------------------------------

        SectionCard(
            Modifier.padding(top = 14.dp)
        ) {

            Text(
                "Calories & macros",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (analysis == null) {

                Text(
                    "Enter a valid portion size for every food to calculate your meal.",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else {

                NutrientRow(
                    "Energy",
                    analysis.calories,
                    "kcal"
                )

                NutrientRow(
                    "Protein",
                    analysis.proteinG,
                    "g"
                )

                NutrientRow(
                    "Carbohydrates",
                    analysis.carbsG,
                    "g"
                )

                NutrientRow(
                    "Fat",
                    analysis.fatG,
                    "g"
                )

                NutrientRow(
                    "Fibre",
                    analysis.fibreG,
                    "g"
                )
            }
        }

        // -----------------------------------------------------
        // VITAMINS AND MINERALS
        // -----------------------------------------------------


        SectionCard(
            Modifier.padding(top = 14.dp)
        ) {

            Text(
                "Vitamins",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (analysis == null) {

                Text(
                    "Enter all portion sizes to view vitamin values.",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else if (analysis.vitamins.isEmpty()) {

                Text(
                    "Vitamin information is unavailable for these foods.",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else {

                val vitaminNames = mapOf(
                    "VitA_RAE" to "Vitamin A",
                    "Thiamin" to "Thiamin (B1)",
                    "Riboflavin" to "Riboflavin (B2)",
                    "Niacin" to "Niacin (B3)",
                    "VitB12" to "Vitamin B12",
                    "VitC" to "Vitamin C"
                )

                analysis.vitamins.forEach { (key, value) ->

                    NutrientRow(
                        vitaminNames[key] ?: key,
                        value,
                        ""
                    )
                }
            }

            Text(
                "Minerals",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(top = 18.dp)
            )

            if (analysis == null) {

                Text(
                    "Mineral information will appear after portion entry.",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else if (analysis.minerals.isEmpty()) {

                Text(
                    "Mineral information is unavailable for these foods.",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )

            } else {

                val mineralNames = mapOf(
                    "Calcium (mg)" to "Calcium",
                    "Iron (mg)" to "Iron",
                    "Magnesium (mg)" to "Magnesium",
                    "Phosphorus (mg)" to "Phosphorus",
                    "Potassium (mg)" to "Potassium",
                    "Sodium (mg)" to "Sodium",
                    "Zinc (mg)" to "Zinc",
                    "Selenium (mcg)" to "Selenium"
                )

                val mineralUnits = mapOf(
                    "Calcium (mg)" to "mg",
                    "Iron (mg)" to "mg",
                    "Magnesium (mg)" to "mg",
                    "Phosphorus (mg)" to "mg",
                    "Potassium (mg)" to "mg",
                    "Sodium (mg)" to "mg",
                    "Zinc (mg)" to "mg",
                    "Selenium (mcg)" to "mcg"
                )

                analysis.minerals.forEach { (key, value) ->

                    NutrientRow(
                        mineralNames[key] ?: key,
                        value,
                        mineralUnits[key] ?: ""
                    )
                }
            }
        }


        // -----------------------------------------------------
        // NUTRITIONAL ADVICE
        // -----------------------------------------------------

        SectionCard(Modifier.padding(top = 14.dp)) {

            Text(
                "Nutrition Feedback",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Text(
                text = analysis?.advice
                    ?: "Enter the portion sizes to view your nutrition feedback.",
                fontSize = 12.sp,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // -----------------------------------------------------
        // CONTINUE TO RATING
        // -----------------------------------------------------

        // ---------------------------------------------------------
// CONTINUE TO RATING - SAVE ANALYSIS TO FIRESTORE
// ---------------------------------------------------------

        PrimaryButton(
            if (isSaving.value) {
                "Saving..."
            } else if (analysis == null) {
                "Enter portions to continue"
            } else {
                "Continue to rating"
            },

            {
                val result = analysis

                if (result != null && !isSaving.value) {

                    isSaving.value = true
                    saveError.value = null

                    coroutineScope.launch {
                        try {
                            // Save nutrition analysis to Firestore.
                            MealRepository().saveAnalysis(flow.mealId, result)
                            //store the result in shared state for RateScreen
                            flow.updateAnalysis(result)
                            // Navigate only after successful saving.
                            onContinue()

                        } catch (e: Exception) {

                            saveError.value =
                                e.localizedMessage
                                    ?: "Could not save your analysis. Please try again."

                        } finally {
                            isSaving.value = false
                        }
                    }
                }
            },

            Modifier.padding(top = 18.dp)
        )

// Display an error if Firestore saving fails.
        if (saveError.value != null) {
            Text(
                text = saveError.value!!,
                fontSize = 12.sp,
                color = FoodOrange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}


// =============================================================
// REUSABLE NUTRIENT ROW
// =============================================================

@Composable
private fun NutrientRow(
    label: String,
    value: Double?,
    unit: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = TextDark
        )

        Text(
            value?.let {
                "%.1f %s".format(it, unit)
            } ?: "Not available",

            fontSize = 13.sp,
            color = FoodGreen,
            fontWeight = FontWeight.Medium
        )
    }
}


// =============================================================
// PLACEHOLDER CARD
// =============================================================

@Composable
private fun PlaceholderCard(
    title: String,
    hint: String
) {

    SectionCard(
        Modifier.padding(top = 14.dp)
    ) {

        Text(
            title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Text(
            hint,
            fontSize = 12.sp,
            color = Muted,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
