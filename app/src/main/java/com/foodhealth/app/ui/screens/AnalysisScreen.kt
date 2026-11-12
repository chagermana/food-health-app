package com.foodhealth.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.NutritionTargets
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.CalorieRing
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.LabeledBar
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.theme.CarbColor
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FatColor
import com.foodhealth.app.ui.theme.FibreColor
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Lime
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.ProteinColor
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark

// =====================================================================
// AnalysisScreen.kt : dark hero card (ring + level bars), a colour-coded
// "Nutrition summary" with grams, a calorie-split bar, and the advice.
// =====================================================================
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
        EmptyState(
            Icons.Outlined.Analytics, "Nothing to analyse yet",
            "Pick or scan a meal and its analysis will show here.", "Choose a meal", onGoHome
        )
        return
    }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val shownKcal by animateIntAsState(if (started) result.calories else 0, tween(900), label = "kcal")
    val ringProgress by animateFloatAsState(
        if (started) (result.calories / NutritionTargets.MEAL_KCAL.toFloat()).coerceIn(0f, 1f) else 0f,
        tween(900), label = "ring"
    )
    val pBar by animateFloatAsState(if (started) (result.proteinG / NutritionTargets.PROTEIN_G.toFloat()).coerceIn(0f, 1f) else 0f, tween(800), label = "p")
    val cBar by animateFloatAsState(if (started) (result.carbsG / NutritionTargets.CARBS_G.toFloat()).coerceIn(0f, 1f) else 0f, tween(800), label = "c")
    val fBar by animateFloatAsState(if (started) (result.fatG / NutritionTargets.FAT_G.toFloat()).coerceIn(0f, 1f) else 0f, tween(800), label = "f")

    // calorie split: protein 4 kcal/g, carbs 4 kcal/g, fat 9 kcal/g
    val pK = result.proteinG * 4f
    val cK = result.carbsG * 4f
    val fK = result.fatG * 9f
    val total = (pK + cK + fK).coerceAtLeast(1f)
    val pPct = (pK / total * 100).toInt()
    val cPct = (cK / total * 100).toInt()
    val fPct = 100 - pPct - cPct

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
            infoText = "Numbers are estimates worked out from the meal, your portion size, how it was cooked and the extras you picked."
        )
        Text(meal.name, fontSize = 13.sp, color = Muted, modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))

        // ---- hero card (dark green, ring + level bars) ----
        val heroShape = RoundedCornerShape(30.dp)
        Row(
            Modifier.fillMaxWidth()
                .shadow(14.dp, heroShape, ambientColor = Color(0x22000000), spotColor = Color(0x55000000))
                .clip(heroShape)
                .background(Brush.linearGradient(listOf(DeepGreen, FoodGreen)))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalorieRing(
                progress = ringProgress,
                centerText = "$shownKcal",
                subText = "of ~${NutritionTargets.MEAL_KCAL} kcal",
                ringSize = 140.dp,
                trackColor = Color(0x33FFFFFF),
                progressColor = Lime,
                textColor = Color.White,
                subColor = Color(0xBBFFFFFF)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LabeledBar("Protein", "${result.proteinG} / ${NutritionTargets.PROTEIN_G} g", pBar, ProteinColor, labelColor = Color.White, trackColor = Color(0x33FFFFFF))
                LabeledBar("Carbs", "${result.carbsG} / ${NutritionTargets.CARBS_G} g", cBar, CarbColor, labelColor = Color.White, trackColor = Color(0x33FFFFFF))
                LabeledBar("Fat", "${result.fatG} / ${NutritionTargets.FAT_G} g", fBar, FatColor, labelColor = Color.White, trackColor = Color(0x33FFFFFF))
            }
        }

        // ---- Nutrition summary (colour-coded grams, like "Personalized Custom Plan") ----
        val cardShape = RoundedCornerShape(28.dp)
        Column(
            Modifier.fillMaxWidth().padding(top = 16.dp)
                .shadow(8.dp, cardShape, ambientColor = Color(0x15000000), spotColor = Color(0x22000000))
                .clip(cardShape)
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFBF3E6))))
                .padding(18.dp)
        ) {
            Text("Nutrition summary", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = TextDark)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                SummaryItem(ProteinColor, "${result.proteinG}g", "Protein", Modifier.weight(1f))
                SummaryItem(CarbColor, "${result.carbsG}g", "Carbs", Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                SummaryItem(FatColor, "${result.fatG}g", "Fat", Modifier.weight(1f))
                SummaryItem(FibreColor, "${result.fibreG}g", "Fibre", Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
            Text("Where your calories come from", fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(8.dp))) {
                Box(Modifier.weight(pK.coerceAtLeast(0.1f)).fillMaxSize().background(ProteinColor))
                Box(Modifier.weight(cK.coerceAtLeast(0.1f)).fillMaxSize().background(CarbColor))
                Box(Modifier.weight(fK.coerceAtLeast(0.1f)).fillMaxSize().background(FatColor))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Protein $pPct%", fontSize = 11.sp, color = ProteinColor, fontWeight = FontWeight.Bold)
                Text("Carbs $cPct%", fontSize = 11.sp, color = CarbColor, fontWeight = FontWeight.Bold)
                Text("Fat $fPct%", fontSize = 11.sp, color = FatColor, fontWeight = FontWeight.Bold)
            }
        }

        // ---- advice ----
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = SoftGreen
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("What this means", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
                Text(
                    result.advice,
                    fontSize = 13.sp, lineHeight = 19.sp, color = TextDark,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        PrimaryButton("Continue", onContinue, Modifier.padding(top = 18.dp))
        Text(
            "Estimates only, based on typical Kenyan recipes and your answers.",
            fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 120.dp)
        )
    }
}

// Colour bar + big grams + label, like the reference.
@Composable
private fun SummaryItem(color: Color, grams: String, label: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(6.dp).height(42.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(grams, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextDark)
            Text(label, fontSize = 12.sp, color = Muted)
        }
    }
}