package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.components.EmptyState
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark

// RateScreen.kt : "How was it?" = star rating + three tips + save button.
@Composable
fun RateScreen(
    flow: MealFlowState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onGoHome: () -> Unit
) {
    val meal = flow.meal
    if (meal == null) {
        EmptyState(
            Icons.Outlined.StarBorder, "Nothing to rate yet",
            "Pick or scan a meal first, then come back to rate it.", "Choose a meal", onGoHome
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
            title = "How was it?",
            onBack = onBack,
            infoText = "Rate the meal from 1 to 5 stars. Your ratings help you see which meals you enjoy and which are good for you."
        )
        MealPhotoCard(
            title = meal.name,
            imageRes = meal.imageRes,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            height = 150.dp,
            labelSize = 13.sp
        )

        SectionCard(Modifier.padding(top = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    val filled = i <= flow.rating
                    Icon(
                        imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "$i star",
                        tint = if (filled) FoodOrange else Line,
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { flow.rating = i }
                    )
                }
            }
        }

        SectionCard(Modifier.padding(top = 12.dp)) {
            Text(
                "Tips for your next meal",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark
            )
            TipRow(Icons.Outlined.Restaurant, "Add a palm-sized portion of beans, eggs or fish for more protein.")
            TipRow(Icons.Outlined.Park, "Keep the greens: they add fibre and iron.")
            TipRow(Icons.Outlined.LocalDrink, "Pair with water or fresh juice instead of sugary soda.")
        }

        PrimaryButton("Save to my diary", onSave, Modifier.padding(top = 20.dp, bottom = 120.dp))
    }
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