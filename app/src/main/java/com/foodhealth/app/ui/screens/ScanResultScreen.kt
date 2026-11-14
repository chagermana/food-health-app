package com.foodhealth.app.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.FoodClass
import com.foodhealth.app.data.MealRepository
import com.foodhealth.app.ml.FoodPrediction
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.CalorieRing
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.FlowPhotoCard
import com.foodhealth.app.ui.components.GlossyIconButton
import com.foodhealth.app.ui.components.LabeledBar
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.components.SmallLabel
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Lime
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.WaterBlue
import com.foodhealth.app.ui.theme.White
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

private const val TAG = "ScanResultScreen"
private const val LOW_CONFIDENCE = 0.5f

// =====================================================================
// ScanResultScreen.kt : shows what the ML model thinks is on the plate
// (ring + top-3 bars), lets the user fix it (dropdown per food, X to
// remove, "Add another food"), then saves the meal and moves on.
// =====================================================================
@Composable
fun ScanResultScreen(
    flow: MealFlowState,
    repository: MealRepository,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onProceed: () -> Unit
) {
    if (!flow.hasMeal) {
        EmptyState(
            Icons.Outlined.CameraAlt, "No meal yet",
            "Scan or upload a photo of your meal first.", "Go to Home", onGoHome
        )
        return
    }

    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val hasPrediction = flow.predictions.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenTopBar(
            title = "Scan result",
            onBack = onBack,
            infoText = "The AI looked at your photo and guessed which foods are on the plate. " +
                    "Check the guesses, fix anything wrong with the dropdowns, then continue."
        )
        FlowPhotoCard(flow, Modifier.fillMaxWidth(), height = 200.dp)

        if (hasPrediction) {
            Text(
                "What the AI sees",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = TextDark,
                modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
            )
            ConfidenceHero(flow.predictions, started)
            Text(
                "Best guesses only. On a plate with several foods the percentages are shared between them.",
                fontSize = 11.sp, color = Muted,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
            if (flow.predictions.first().probability < LOW_CONFIDENCE) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = SoftGreen
                ) {
                    Text(
                        "I'm not fully sure about this one. Please check the foods below and fix anything that looks wrong.",
                        fontSize = 13.sp, lineHeight = 18.sp, color = TextDark,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // ---------- the editable plate ----------
        SectionCard(Modifier.padding(top = 16.dp)) {
            Text(
                "Your plate",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark
            )
            SmallLabel(
                "Tap a food to change it. Tap ✕ to remove it.",
                Modifier.padding(top = 2.dp)
            )
            flow.selected.forEachIndexed { index, food ->
                key(food) {
                    FoodRow(
                        food = food,
                        hasPrediction = hasPrediction,
                        options = flow.optionsFor(index),
                        probabilityOf = { flow.probabilityOf(it) },
                        canRemove = flow.selected.size > 1,
                        onPick = { flow.changeFood(index, it) },
                        onRemove = { flow.removeFood(index) }
                    )
                }
            }
            if (flow.selected.size < MealFlowState.MAX_FOODS) {
                Surface(
                    onClick = { flow.addFood() },
                    shape = CircleShape,
                    color = SoftGreen,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, tint = FoodGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add another food", fontSize = 13.sp, color = FoodGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        errorText?.let {
            Text(
                it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp)
            )
        }

        PrimaryButton(
            text = if (saving) "Saving..." else "Proceed to nutrient analysis",
            enabled = !saving,
            modifier = Modifier.padding(top = 18.dp, bottom = 120.dp),
            onClick = {
                scope.launch {
                    saving = true
                    errorText = null
                    try {
                        withTimeout(10_000) {
                            repository.saveScannedMeal(
                                mealId = flow.mealId,
                                source = flow.source,
                                predictions = flow.predictions,
                                confirmed = flow.selected,
                                edited = flow.wasEdited
                            )
                        }
                        flow.markSaved()
                        onProceed()
                    } catch (e: TimeoutCancellationException) {
                        errorText = "Saving is taking too long. Check your connection and try again."
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not save meal", e)
                        errorText = "Couldn't save your meal. Please try again."
                    } finally {
                        saving = false
                    }
                }
            }
        )
    }
}

// Dark green card: ring for the best guess + bars for the top 3 (glossy, like Analysis).
@Composable
private fun ConfidenceHero(predictions: List<FoodPrediction>, started: Boolean) {
    val top = predictions.take(3)
    val first = top.first()
    val shape = RoundedCornerShape(30.dp)
    val ring by animateFloatAsState(if (started) first.probability else 0f, tween(900), label = "ring")
    val barColors = listOf(Lime, FoodOrange, WaterBlue)

    Row(
        Modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Color(0x22000000), spotColor = Color(0x55000000))
            .clip(shape)
            .background(Brush.linearGradient(listOf(DeepGreen, FoodGreen)))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalorieRing(
            progress = ring,
            centerText = "${(first.probability * 100).roundToInt()}%",
            subText = first.food.displayName,
            ringSize = 140.dp,
            trackColor = Color(0x33FFFFFF),
            progressColor = Lime,
            textColor = Color.White,
            subColor = Color(0xBBFFFFFF)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            top.forEachIndexed { i, p -> ConfidenceBar(p, barColors[i], started) }
        }
    }
}

@Composable
private fun ConfidenceBar(p: FoodPrediction, color: Color, started: Boolean) {
    val progress by animateFloatAsState(
        if (started) p.probability.coerceIn(0f, 1f) else 0f, tween(800), label = "bar"
    )
    LabeledBar(
        label = p.food.displayName,
        valueText = "%.1f%%".format(p.probability * 100f),
        progress = progress,
        color = color,
        labelColor = Color.White,
        trackColor = Color(0x33FFFFFF)
    )
}

// One food on the plate: a dropdown field (change it) and an X (remove it).
@Composable
private fun FoodRow(
    food: FoodClass,
    hasPrediction: Boolean,
    options: List<FoodClass>,
    probabilityOf: (FoodClass) -> Float,
    canRemove: Boolean,
    onPick: (FoodClass) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val detail = buildString {
        append(if (hasPrediction) "${pct(probabilityOf(food))} match" else "Picked by you")
        if (!food.hasNutrientData) append(" · nutrient data coming soon")
    }

    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = BorderStroke(1.5.dp, Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(food.displayName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                        Text(detail, fontSize = 11.sp, color = Muted)
                    }
                    Icon(Icons.Filled.ArrowDropDown, "Change food", tint = FoodGreen)
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
                                option.displayName,
                                fontWeight = if (option == food) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingIcon = {
                            if (hasPrediction) {
                                Text(pct(probabilityOf(option)), fontSize = 12.sp, color = Muted)
                            }
                        },
                        onClick = {
                            expanded = false
                            onPick(option)
                        }
                    )
                }
            }
        }
        if (canRemove) {
            Spacer(Modifier.width(8.dp))
            GlossyIconButton(
                icon = Icons.Filled.Close,
                description = "Remove ${food.displayName}",
                onClick = onRemove,
                size = 40.dp
            )
        }
    }
}

private fun pct(p: Float): String {
    val v = (p * 100).roundToInt()
    return if (v == 0 && p > 0f) "<1%" else "$v%"
}