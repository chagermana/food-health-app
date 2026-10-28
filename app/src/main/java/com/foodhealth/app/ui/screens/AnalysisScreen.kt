package com.foodhealth.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.NutritionTargets
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.BackHeader
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodGreenLight
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark

// =====================================================================
// AnalysisScreen.kt : "Your meal analysis" = calorie ring, macro bars and a
// short sentence of advice. It shows the numbers worked out in
@Composable
fun AnalysisScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
    val meal = flow.meal
    val result = flow.result
    if (meal == null || result == null) {
        // The Analysis TAB can be opened before any meal was picked.
        EmptyState(
            Icons.Outlined.Analytics, "Nothing to analyse yet",
            "Pick or scan a meal and its analysis will show here.", "Choose a meal", onGoHome
        )
        return
    }

    // The bars and ring animate from 0 to their value when the screen opens.
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val shownKcal by animateIntAsState(
        targetValue = if (started) result.calories else 0,
        animationSpec = tween(900), label = "kcal"
    )
    val ringProgress by animateFloatAsState(
        targetValue = if (started)
            (result.calories / NutritionTargets.MEAL_KCAL.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(900), label = "ring"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        BackHeader(onBack)
        Text(
            "Your meal analysis",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextDark
        )
        Text(meal.name, fontSize = 13.sp, color = Muted)

        // Calorie ring in the middle
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            CalorieRing(progress = ringProgress, kcal = shownKcal)
        }
        Text(
            "of ~${NutritionTargets.MEAL_KCAL} kcal suggested for this meal",
            fontSize = 11.sp, color = Muted,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        // Macro bars
        SectionCard(Modifier.padding(top = 12.dp)) {
            NutrientRow("Protein", result.proteinG, NutritionTargets.PROTEIN_G, started)
            NutrientRow("Carbohydrates", result.carbsG, NutritionTargets.CARBS_G, started)
            NutrientRow("Fats", result.fatG, NutritionTargets.FAT_G, started)
            NutrientRow("Fibre", result.fibreG, NutritionTargets.FIBRE_G, started)
        }

        // "What this means": the advice sentence (if/else rule in data/Meal.kt)
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = SoftGreen
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "What this means",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    result.advice,
                    fontSize = 13.sp, lineHeight = 19.sp, color = TextDark,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        PrimaryButton("Continue", onContinue, Modifier.padding(top = 16.dp))
        Text(
            "Estimates only, based on typical Kenyan recipes and your answers.",
            fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        )
    }
}

// The round progress ring with the calorie number inside.
@Composable
private fun CalorieRing(progress: Float, kcal: Int) {
    Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 14.dp.toPx()
            // grey full circle (the track)
            drawArc(
                color = Line, startAngle = -90f, sweepAngle = 360f,
                useCenter = false, style = Stroke(strokeWidth)
            )
            // green part: starts at the top (-90 degrees), grows clockwise
            drawArc(
                color = FoodGreen, startAngle = -90f, sweepAngle = 360f * progress,
                useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$kcal",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 30.sp, color = TextDark
            )
            Text("kcal", fontSize = 11.sp, color = Muted)
        }
    }
}

// One line: "Protein ........ 28g" plus a progress bar under it.
@Composable
private fun NutrientRow(label: String, grams: Int, target: Int, started: Boolean) {
    val progress by animateFloatAsState(
        targetValue = if (started) (grams / target.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800), label = "bar_$label"
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextDark)
        Text("${grams}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(9.dp)),
        color = FoodGreenLight,
        trackColor = Line
    )
}