package com.foodhealth.app.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.foodhealth.app.data.MealRepository
import com.foodhealth.app.data.StoredMeal
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.util.DateKeys
import com.foodhealth.app.util.DayChip
import kotlinx.coroutines.CancellationException

private const val TAG = "MealDiarySection"

// =====================================================================
// MealDiarySection.kt : "Meal diary" card for the Profile screen.
// A strip with the last 7 days, plus a calendar button for older dates.
// Reads ONE day at a time from Firestore (cheap on the free plan).
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDiarySection(modifier: Modifier = Modifier) {
    val repository = remember { MealRepository() }
    val days = remember { DateKeys.lastDays(7) }

    var selectedKey by rememberSaveable { mutableStateOf(DateKeys.today()) }
    var meals by remember { mutableStateOf<List<StoredMeal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var showPicker by remember { mutableStateOf(false) }

    // Runs again whenever the chosen day changes (or "Try again" is tapped).
    LaunchedEffect(selectedKey, reloadTick) {
        loading = true
        failed = false
        try {
            meals = repository.mealsForDay(selectedKey)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Could not load diary", e)
            meals = emptyList()
            failed = true
        } finally {
            loading = false
        }
    }

    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFCF8F0))))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Meal diary",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                color = TextDark, modifier = Modifier.weight(1f)
            )
            Text(
                DateKeys.label(selectedKey),
                fontSize = 12.sp, color = FoodOrange, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(SoftGreen).clickable { showPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CalendarMonth, "Pick a date", tint = FoodGreen, modifier = Modifier.size(18.dp))
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { day ->
                DayChipView(day, day.key == selectedKey, Modifier.weight(1f)) { selectedKey = day.key }
            }
        }

        Spacer(Modifier.height(12.dp))
        when {
            loading -> Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FoodGreen, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
            }
            failed -> Column {
                Text("Couldn't load your diary. Check your connection.", fontSize = 12.sp, color = Muted)
                TextButton(onClick = { reloadTick++ }) { Text("Try again", color = FoodGreen) }
            }
            meals.isEmpty() -> Text("No meals logged on this day.", fontSize = 13.sp, color = Muted)
            else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meals.forEach { DiaryRow(it) }
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateKeys.utcMillisFor(selectedKey)
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedKey = DateKeys.keyFromUtcMillis(it) }
                    showPicker = false
                }) { Text("OK", color = FoodGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel", color = Muted) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun DayChipView(day: DayChip, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) FoodGreen else Color(0xFFF3EFE6))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            day.weekday.take(3), fontSize = 10.sp, textAlign = TextAlign.Center,
            color = if (selected) Color.White else Muted
        )
        Text(
            day.dayNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextDark
        )
    }
}

@Composable
private fun DiaryRow(meal: StoredMeal) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F4EC))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            DateKeys.timeLabel(meal.createdAtMillis),
            fontSize = 12.sp, color = Muted, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                meal.foodNames.joinToString(" + ").ifEmpty { "Meal" },
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark
            )
            Text(
                if (meal.analyzed) "Analysed" else "Not analysed yet",
                fontSize = 11.sp, color = if (meal.analyzed) FoodGreen else Muted
            )
        }
        if (meal.rating > 0) {
            Icon(Icons.Filled.Star, null, tint = FoodOrange, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp))
            Text("${meal.rating}", fontSize = 12.sp, color = TextDark)
        }
    }
}