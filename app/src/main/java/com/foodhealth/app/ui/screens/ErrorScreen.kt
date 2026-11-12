package com.foodhealth.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.TextDark
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.PeachDeep

// =====================================================================
// ErrorScreen.kt : "Whoops! Data hiccup" EXAMPLE LAYOUT
// =====================================================================
@Composable
fun ErrorScreen(onRetry: () -> Unit, onGoHome: () -> Unit) {
    Box(                                              // <-- NEW
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PeachDeep, Peach, Cream, Cream)))
    ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(12.dp, CircleShape, ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Restaurant, contentDescription = null,
                tint = FoodGreen, modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Whoops! Data hiccup",
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = 24.sp, color = TextDark, textAlign = TextAlign.Center)
        Text(
            "We seem to be missing some delicious data. Check your connection and try again.",
            fontSize = 13.sp, lineHeight = 19.sp, color = Muted, textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        PrimaryButton("Retry connection", onRetry)
        TextButton(onClick = onGoHome) { Text("Go back home", color = FoodGreen) }
    }
}
}

