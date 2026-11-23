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
import androidx.compose.ui.graphics.Color
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
import com.foodhealth.app.ui.components.LabeledBar
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.components.SmallLabel
import com.foodhealth.app.ui.theme.CarbColor
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.FatColor
import com.foodhealth.app.ui.theme.FibreColor
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.ProteinColor
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark
import kotlin.math.abs

// RateScreen.kt : "How well did you do?"
// Ten stars = the meal's balance score (0..10) from MealBalanceScorer.
// Each of 5 nutrients earns up to 2 points for being close to its per-meal target.
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
    val score = analysis.balanceScore      // 0..10, or null if data is missing
    val stars = score ?: 0

    // The score is what gets saved to the diary (0 = not saved).
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
            infoText = "Your score out of 10 shows how close this meal is to a balanced plate: calories, protein, carbs, fat and fibre."
        )
        FlowPhotoCard(flow, Modifier.fillMaxWidth().padding(top = 4.dp), height = 150.dp)

        // ---------- ten-star rating ----------
        SectionCard(Modifier.padding(top = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                for (i in 1..10) {
                    Icon(
                        imageVector = if (i <= stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (i <= stars) FoodOrange else Line,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }
            Text(
                verdictFor(score),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = TextDark
            )
            SmallLabel(
                if (score != null) "Your score: $score / 10"
                else "Not enough nutrient data to score this meal.",
                Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        // ---------- how the score is worked out ----------
        if (score != null) {
            val lines = listOfNotNull(
                analysis.calories?.let { ScoreLine("Calories", it, NutritionTargets.MEAL_KCAL, "kcal", FoodOrange) },
                analysis.proteinG?.let { ScoreLine("Protein", it, NutritionTargets.PROTEIN_G, "g", ProteinColor) },
                analysis.carbsG?.let { ScoreLine("Carbs", it, NutritionTargets.CARBS_G, "g", CarbColor) },
                analysis.fatG?.let { ScoreLine("Fat", it, NutritionTargets.FAT_G, "g", FatColor) },
                analysis.fibreG?.let { ScoreLine("Fibre", it, NutritionTargets.FIBRE_G, "g", FibreColor) }
            )
            SectionCard(Modifier.padding(top = 12.dp)) {
                Text(
                    "How your score is worked out",
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark
                )
//                SmallLabel(
//                    "Each nutrient earns up to 2 points for being close to the target for one meal. " +
//                            "The closer you are, the more points. The five add up to 10.",
//                    Modifier.padding(top = 4.dp, bottom = 12.dp)
//                )
                lines.forEach { line ->
                    val pts = line.points()
                    LabeledBar(
                        label = "${line.label} · %.0f of %d %s".format(line.actual, line.target, line.unit),
                        valueText = "%.1f / 2".format(pts),
                        progress = (pts / 2.0).toFloat(),
                        color = line.color
                    )
                }
                SmallLabel(
                    "Total: %.1f, rounded to $score / 10".format(lines.sumOf { it.points() }),
                    Modifier.padding(top = 2.dp)
                )
            }
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

// One nutrient's share of the score. Same rule as MealBalanceScorer:
// 2 points x (1 - how far off the target you are, as a fraction), kept between 0 and 2.
private data class ScoreLine(
    val label: String,
    val actual: Double,
    val target: Int,
    val unit: String,
    val color: Color
) {
    fun points(): Double = (2.0 * (1.0 - abs(actual - target) / target)).coerceIn(0.0, 2.0)
}

private fun verdictFor(score: Int?): String = when {
    score == null -> "No rating for this meal"
    score >= 9 -> "Excellent balance!"
    score >= 7 -> "Great meal!"
    score >= 5 -> "Good start"
    score >= 3 -> "Could be better"
    else -> "Needs some work"
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