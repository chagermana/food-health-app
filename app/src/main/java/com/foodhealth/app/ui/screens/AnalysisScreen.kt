package com.foodhealth.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.FoodClass
import com.foodhealth.app.data.FoodPortion
import com.foodhealth.app.data.MealRepository
import com.foodhealth.app.data.NutritionCalculator
import com.foodhealth.app.data.NutritionRepository
import com.foodhealth.app.data.PortionOptions
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.FlowPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun AnalysisScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
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

    val context = LocalContext.current
    val nutritionRepository = remember(context) { NutritionRepository(context) }
    val nutritionCalculator = remember(nutritionRepository) { NutritionCalculator(nutritionRepository) }

    // Grams chosen from the dropdowns (stored as text, same as before).
    val portionInputs = flow.portionInputs

    val portions = flow.selected.mapNotNull { food ->
        val grams = portionInputs[food.label]?.toDoubleOrNull()
        if (grams != null && grams > 0) FoodPortion(food, grams) else null
    }
    val allPortionsEntered = portions.size == flow.selected.size && portions.isNotEmpty()

    val analysis = remember(portionInputs.toMap(), flow.selected) {
        if (allPortionsEntered) nutritionCalculator.calculate(portions) else null
    }

    val coroutineScope = rememberCoroutineScope()
    val isSaving = remember(flow.mealId) { mutableStateOf(false) }
    val saveError = remember(flow.mealId) { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
    ) {
        // ---------- scrolling content ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            ScreenTopBar(
                title = "Meal analysis",
                onBack = onBack,
                infoText = "Pick roughly how much of each food you ate. Your calories, macros, vitamins and minerals appear below."
            )

            FlowPhotoCard(flow, Modifier.fillMaxWidth(), height = 140.dp)

            // ---------- confirmed foods ----------
            SectionCard(Modifier.padding(top = 14.dp)) {
                Text("Foods in this meal", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
                flow.selected.forEach { food ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(food.displayName, modifier = Modifier.weight(1f), fontSize = 14.sp, color = TextDark)
                        Text(
                            if (food.hasNutrientData) "Nutrient data ready" else "Placeholder data",
                            fontSize = 11.sp,
                            color = if (food.hasNutrientData) FoodGreen else FoodOrange
                        )
                    }
                }
            }

            // ---------- portion dropdowns ----------
            SectionCard(Modifier.padding(top = 14.dp)) {
                Text("Portion sizes", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
                Text(
                    "Choose the portion that is closest to what you ate.",
                    fontSize = 12.sp, color = Muted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                )
                flow.selected.forEach { food ->
                    PortionRow(
                        food = food,
                        selectedGrams = portionInputs[food.label] ?: "",
                        onPick = { grams -> portionInputs[food.label] = grams.toString() }
                    )
                }
            }

            // ---------- calories and macros ----------
            SectionCard(Modifier.padding(top = 14.dp)) {
                Text("Calories & macros", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
                if (analysis == null) {
                    Text(
                        "Choose a portion for every food to calculate your meal.",
                        fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    NutrientRow("Energy", analysis.calories, "kcal")
                    NutrientRow("Protein", analysis.proteinG, "g")
                    NutrientRow("Carbohydrates", analysis.carbsG, "g")
                    NutrientRow("Fat", analysis.fatG, "g")
                    NutrientRow("Fibre", analysis.fibreG, "g")
                }
            }

            // ---------- vitamins and minerals ----------
            SectionCard(Modifier.padding(top = 14.dp)) {
                Text("Vitamins", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)

                if (analysis == null) {
                    Text(
                        "Choose all portions to view vitamin values.",
                        fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp)
                    )
                } else if (analysis.vitamins.isEmpty()) {
                    Text(
                        "Vitamin information is unavailable for these foods.",
                        fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp)
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
                        NutrientRow(vitaminNames[key] ?: key, value, "")
                    }
                }

                Text(
                    "Minerals", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    color = TextDark, modifier = Modifier.padding(top = 18.dp)
                )

                if (analysis == null) {
                    Text(
                        "Mineral information will appear after you choose portions.",
                        fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp)
                    )
                } else if (analysis.minerals.isEmpty()) {
                    Text(
                        "Mineral information is unavailable for these foods.",
                        fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp)
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
                        NutrientRow(mineralNames[key] ?: key, value, mineralUnits[key] ?: "")
                    }
                }
            }

            // ---------- advice ----------
            SectionCard(Modifier.padding(top = 14.dp)) {
                Text("Nutrition Feedback", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
                Text(
                    text = analysis?.advice ?: "Choose your portions to view your nutrition feedback.",
                    fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Room so the last card is not hidden behind the pinned button + bottom bar.
            Spacer(Modifier.height(210.dp))
        }

        // ---------- pinned button, sits just above the floating bottom bar ----------
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 96.dp)
        ) {
            saveError.value?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = FoodOrange,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            PrimaryButton(
                text = when {
                    isSaving.value -> "Saving..."
                    analysis == null -> "Choose portions to continue"
                    else -> "Continue to rating"
                },
                enabled = analysis != null && !isSaving.value,
                onClick = {
                    val result = analysis
                    if (result != null && !isSaving.value) {
                        isSaving.value = true
                        saveError.value = null
                        coroutineScope.launch {
                            try {
                                MealRepository().saveAnalysis(
                                    flow.mealId,
                                    result,
                                    portions.associate { it.food.label to it.grams.toInt() }
                                )
                                flow.updateAnalysis(result)
                                onContinue()
                            } catch (e: Exception) {
                                saveError.value = e.localizedMessage
                                    ?: "Could not save your analysis. Please try again."
                            } finally {
                                isSaving.value = false
                            }
                        }
                    }
                }
            )
        }
    }
}

// One food + its portion dropdown.
@Composable
private fun PortionRow(
    food: FoodClass,
    selectedGrams: String,
    onPick: (Int) -> Unit
) {
    val options = remember(food) { PortionOptions.forFood(food) }
    val selected = options.firstOrNull { it.grams.toString() == selectedGrams }
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(0.8f)) {
            Text(food.displayName, fontWeight = FontWeight.Medium, color = TextDark, fontSize = 14.sp)
            Text("Pick a portion", fontSize = 11.sp, color = Muted)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1.2f)) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = BorderStroke(1.5.dp, if (selected != null) FoodGreen else Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selected?.display ?: "Select portion",
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontWeight = if (selected != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected != null) TextDark else Muted
                    )
                    Icon(Icons.Filled.ArrowDropDown, "Change portion", tint = FoodGreen)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(18.dp),
                containerColor = White
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.display,
                                fontSize = 13.sp,
                                fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            expanded = false
                            onPick(option.grams)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NutrientRow(label: String, value: Double?, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = TextDark)
        Text(
            value?.let { "%.1f %s".format(it, unit) } ?: "Not available",
            fontSize = 13.sp,
            color = FoodGreen,
            fontWeight = FontWeight.Medium
        )
    }
}