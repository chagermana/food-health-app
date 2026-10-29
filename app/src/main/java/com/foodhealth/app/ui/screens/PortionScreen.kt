package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.CookedMethod
import com.foodhealth.app.data.Extra
import com.foodhealth.app.data.PortionSize
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.ChoiceChip
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.components.SmallLabel
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.TextDark

// Questions on chosen meal
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortionScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onAnalyse: () -> Unit
) {
    val meal = flow.meal
    if (meal == null) {
        EmptyState(
            Icons.Outlined.Restaurant, "No meal selected",
            "Pick or scan a meal first.", "Go to Home", onGoHome
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
            title = "Your meal",
            onBack = onBack,
            infoText = "Tell us how big the portion was, how it was cooked and what you added. This makes the estimate more accurate."
        )
        MealPhotoCard(
            title = meal.name,
            imageRes = meal.imageRes,
            modifier = Modifier.fillMaxWidth(),
            height = 180.dp,
            labelSize = 14.sp
        )
        Text(
            "A few quick questions",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextDark,
            modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
        )

        SectionCard {
            SmallLabel("Portion size")
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PortionSize.entries.forEach { option ->
                    ChoiceChip(option.label, flow.size == option) { flow.size = option }
                }
            }
        }

        SectionCard(Modifier.padding(top = 12.dp)) {
            SmallLabel("How was it cooked?")
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CookedMethod.entries.forEach { option ->
                    ChoiceChip(option.label, flow.cook == option) { flow.cook = option }
                }
            }
        }

        SectionCard(Modifier.padding(top = 12.dp)) {
            SmallLabel("Extras (tap again to remove)")
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Extra.entries.forEach { option ->
                    val selected = option in flow.extras
                    ChoiceChip(option.label, selected) {
                        flow.extras = if (selected) flow.extras - option else flow.extras + option
                    }
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