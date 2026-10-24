package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.SampleMeals
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodGreenLight
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.TextDark
import com.google.firebase.auth.FirebaseAuth

// =====================================================================
// ProfileScreen.kt : PLACEHOLDER layout
// =====================================================================
@Composable
fun ProfileScreen(onLogout: () -> Unit, onPreviewError: () -> Unit) {
    val email = remember { FirebaseAuth.getInstance().currentUser?.email ?: "guest@dishipoa" }
    val displayName = remember(email) { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
    val initials = remember(displayName) { displayName.take(2).uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // --- avatar + name ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(68.dp).clip(CircleShape).background(FoodGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = androidx.compose.ui.graphics.Color.White,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(displayName, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontSize = 20.sp, color = TextDark)
                Text(email, fontSize = 12.sp, color = Muted)   // TODO: age + goal from Firestore
            }
        }

        // --- weekly bars (static example, Monday to Sunday) ---
        SectionCard(Modifier.padding(top = 16.dp)) {
            Text("This week", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                color = TextDark)
            WeeklyBars(
                values = listOf(60, 80, 45, 90, 70, 55, 75),   // TODO: real daily totals
                days = listOf("M", "T", "W", "T", "F", "S", "S")
            )
        }

        // --- top 3 meals (static) ---
        Text("Top 3 meals", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SampleMeals.all.take(3).forEach { meal ->
                MealPhotoCard(
                    title = meal.name.substringBefore(" "),
                    imageRes = meal.imageRes,
                    modifier = Modifier.weight(1f),
                    height = 84.dp,
                    labelSize = 10.sp
                )
            }
        }

        // --- buttons ---
        OutlinedButton(
            onClick = onPreviewError,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp)
        ) { Text("Preview error screen") }   // TODO: remove before the final APK

        Button(
            onClick = onLogout,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log out", fontWeight = FontWeight.Bold)
        }
    }
}

// Seven vertical bars. The last one (today) is saffron, as in the prototype.
@Composable
private fun WeeklyBars(values: List<Int>, days: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth().height(110.dp).padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((value * 0.7f).dp)    // value 0..100 -> height in dp
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (index == values.lastIndex) FoodOrange else FoodGreenLight)
                )
                Text(days[index], fontSize = 10.sp, color = Muted,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
