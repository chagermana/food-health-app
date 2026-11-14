package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// =====================================================================
// AnalysisScreen.kt : PLACEHOLDER for the nutrient-analysis teammate.
//
// What you can use here:
//   flow.selected  -> the foods the user confirmed (List<FoodClass>)
//   flow.mealId    -> id of the meal already saved in users/{uid}/meals
//   FoodClass.csvCode / hasNutrientData -> which CSV row to look up
//
// TODO(teammate): 1) look up nutrients (per 100 g) for each food,
//                 2) replace the three placeholder cards below,
//                 3) call MealRepository().saveAnalysis(flow.mealId, MealAnalysis(...))
//                    inside a coroutine. See NUTRIENT_ANALYSIS_HANDOVER.txt.
// =====================================================================
@Composable
fun AnalysisScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
    if (!flow.readyForAnalysis) {
        EmptyState(
            Icons.Outlined.Analytics, "Nothing to analyse yet",
            "Scan or upload a meal and confirm the foods. The analysis will show here.",
            "Choose a meal", onGoHome
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenTopBar(
            title = "Meal analysis",
            onBack = onBack,
            infoText = "Your calories, macros, vitamins and minerals for this meal will appear here."
        )
        FlowPhotoCard(flow, Modifier.fillMaxWidth(), height = 140.dp)

        SectionCard(Modifier.padding(top = 14.dp)) {
            Text("Foods in this meal", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
            flow.selected.forEach { food ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
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

        PlaceholderCard("Calories & macros", "Calories, protein, carbs, fat and fibre will appear here.")
        PlaceholderCard("Vitamins & minerals", "Iron, calcium, vitamin A, vitamin C and more will appear here.")
        PlaceholderCard("What this means", "Simple advice based on your meal will appear here.")

        PrimaryButton("Continue to rating", onContinue, Modifier.padding(top = 18.dp))
        Text(
            "Nutrient analysis is coming soon.",
            fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 120.dp)
        )
    }
}

@Composable
private fun PlaceholderCard(title: String, hint: String) {
    SectionCard(Modifier.padding(top = 14.dp)) {
        Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
        Text(hint, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 6.dp))
    }
}