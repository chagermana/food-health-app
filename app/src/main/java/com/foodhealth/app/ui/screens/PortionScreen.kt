package com.foodhealth.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.CookedMethod
import com.foodhealth.app.data.Extra
import com.foodhealth.app.data.PortionSize
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.BackHeader
import com.foodhealth.app.ui.components.ChoiceChip
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.SmallLabel
import com.foodhealth.app.ui.theme.TextDark

// =====================================================================
// PortionScreen.kt : "A few quick questions" about the chosen meal.
// =====================================================================
@OptIn(ExperimentalLayoutApi::class)   // FlowRow = chips that wrap onto a new line
@Composable
fun PortionScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onAnalyse: () -> Unit
) {
    val meal = flow.meal
    if (meal == null) {
        // Safety net: nobody should reach this screen without a meal.
        EmptyState(
            Icons.Outlined.Restaurant, "No meal selected",
            "Pick or scan a meal first.", "Go to Home", onGoHome
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())    // scroll on small phones
            .padding(horizontal = 20.dp)
    ) {
        BackHeader(onBack)
        MealPhotoCard(
            title = meal.name,
            imageRes = meal.imageRes,
            modifier = Modifier.fillMaxWidth(),
            height = 170.dp,
            labelSize = 14.sp
        )
        Text(
            "A few quick questions",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextDark,
            modifier = Modifier.padding(top = 16.dp)
        )

        // --- Portion size: only one can be chosen ---
        SmallLabel("Portion size", Modifier.padding(top = 12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PortionSize.entries.forEach { option ->
                ChoiceChip(option.label, flow.size == option) { flow.size = option }
            }
        }

        // --- Cooking method: only one can be chosen ---
        SmallLabel("How was it cooked?", Modifier.padding(top = 12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CookedMethod.entries.forEach { option ->
                ChoiceChip(option.label, flow.cook == option) { flow.cook = option }
            }
        }

        // --- Extras: any number can be chosen (tap again to remove) ---
        SmallLabel("Extras", Modifier.padding(top = 12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Extra.entries.forEach { option ->
                val selected = option in flow.extras
                ChoiceChip(option.label, selected) {
                    flow.extras = if (selected) flow.extras - option else flow.extras + option
                }
            }
        }

        PrimaryButton(
            text = "Analyse my meal",
            onClick = onAnalyse,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )
    }
}