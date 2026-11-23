package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.MealAnalysis
import com.foodhealth.app.data.NutritionTargets
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.FlowPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.components.SmallLabel
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark
import kotlin.math.roundToInt

// RateScreen.kt : "How well did you do?"
// The stars are worked out from the meal's balance score (0..10 -> 1..5 stars).
// The user does not rate themselves any more.
@Composable
fun RateScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onGoHome: () -> Unit
) {
    if (!flow.readyForAnalysis || flow.analysis == null) {
        EmptyState(
            Icons.Outlined.StarBorder, "Nothing to rate yet",
            "Scan or pick a meal, confirm the foods and choose your portions, then come back to see how well you did.",
            "Choose a meal", onGoHome
        )
        return
    }

    val analysis = flow.analysis!!
    val score = analysis.balanceScore
    val stars = score?.let { (it / 2.0).roundToInt().coerceIn(1, 5) } ?: 0

    // The automatic rating is what gets saved to the diary.
    LaunchedEffect(stars) { flow.rating = stars }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenTopBar(
            title = "How well did you do?",
            onBack = onBack,
            infoText = "Your rating is worked out from how close this meal is to a balanced plate: calories, protein, carbs, fat and fibre."
        )
        FlowPhotoCard(flow, Modifier.fillMaxWidth().padding(top = 4.dp), height = 150.dp)

        // ---------- automatic star rating ----------
        SectionCard(Modifier.padding(top = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (i <= stars) FoodOrange else Line,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Text(
                verdictFor(stars),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = TextDark
            )
            SmallLabel(
                if (score != null) "Balanced-diet score: $stars/ 5"
                else "Not enough nutrient data to score this meal.",
                Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        // ---------- tips from the real numbers ----------
        val tips = buildTips(analysis)
        SectionCard(Modifier.padding(top = 12.dp)) {
            Text(
                "Tips for your next meal",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark
            )
            tips.forEach { TipRow(it.icon, it.text) }
        }

        PrimaryButton("Save to my diary", onSave, Modifier.padding(top = 20.dp, bottom = 120.dp))
    }
}

private fun verdictFor(stars: Int): String = when (stars) {
    5 -> "Excellent balance!"
    4 -> "Great meal!"
    3 -> "Good start"
    2 -> "Could be better"
    1 -> "Needs some work"
    else -> "No rating for this meal"
}

private data class Tip(val icon: ImageVector, val text: String)

// Compares the meal with the per-meal targets in NutritionTargets.
private fun buildTips(a: MealAnalysis): List<Tip> {
    val tips = mutableListOf<Tip>()

    a.proteinG?.let {
        if (it < NutritionTargets.PROTEIN_G * 0.7)
            tips += Tip(Icons.Outlined.Restaurant, "Low in protein. Add a palm-sized portion of beans, eggs, fish or meat.")
    }
    a.fibreG?.let {
        if (it < NutritionTargets.FIBRE_G * 0.6)
            tips += Tip(Icons.Outlined.Park, "Low in fibre. Add sukuma wiki, kachumbari or other vegetables.")
    }
    a.fatG?.let {
        if (it > NutritionTargets.FAT_G * 1.3)
            tips += Tip(Icons.Outlined.WaterDrop, "High in fat. Try grilled or boiled foods instead of fried ones.")
    }
    a.carbsG?.let {
        if (it > NutritionTargets.CARBS_G * 1.3)
            tips += Tip(Icons.Outlined.Grain, "A lot of carbohydrates. Try a smaller portion of ugali, rice or chips.")
    }
    a.calories?.let {
        if (it > NutritionTargets.MEAL_KCAL * 1.3)
            tips += Tip(Icons.Outlined.LocalFireDepartment, "This meal is high in calories. A smaller portion would sit closer to a balanced meal.")
        else if (it < NutritionTargets.MEAL_KCAL * 0.5)
            tips += Tip(Icons.Outlined.LocalFireDepartment, "This is a light meal. You may need a side or snack to stay full.")
    }

    if (tips.isEmpty()) {
        tips += Tip(Icons.Outlined.CheckCircle, "Nicely balanced. Keep it up, and drink water with your meal.")
    }
    return tips.take(3)
}

@Composable
private fun TipRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = SoftGreen) {
            Icon(icon, contentDescription = null, tint = FoodGreen, modifier = Modifier.padding(6.dp).size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, lineHeight = 18.sp, color = TextDark)
    }
}