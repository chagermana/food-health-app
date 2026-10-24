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

// =====================================================================
// ErrorScreen.kt : "Whoops! Data hiccup" EXAMPLE LAYOUT
// =====================================================================
@Composable
fun ErrorScreen(onRetry: () -> Unit, onGoHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = FoodGreen,
            modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
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
