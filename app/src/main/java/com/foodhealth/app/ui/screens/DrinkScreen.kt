package com.foodhealth.app.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.R
import com.foodhealth.app.data.DrinkRepository
import com.foodhealth.app.data.Drinks
import com.foodhealth.app.ui.components.GlossyIconButton
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.PrimaryButton
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.components.SectionCard
import com.foodhealth.app.ui.components.SmallLabel
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.White
import com.foodhealth.app.util.drawableByName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

private const val TAG = "DrinkScreen"
private const val MAX_CUPS = 12

// DrinkScreen.kt : log a drink. Pick the drink (dropdown), choose how many
// glasses, save. Saved to Firestore and shown on the Profile page.
@Composable
fun DrinkScreen(
    initialDrinkId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { DrinkRepository() }
    val scope = rememberCoroutineScope()

    var drinkId by rememberSaveable { mutableStateOf(initialDrinkId) }
    var cups by rememberSaveable { mutableIntStateOf(1) }
    var expanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val drink = Drinks.byId(drinkId) ?: Drinks.all.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenTopBar(
            title = "Log a drink",
            onBack = onBack,
            infoText = "Choose the drink and how many glasses you had. One glass is about 250 ml. " +
                    "Drinks are added to your profile and do not change your meal analysis."
        )

        MealPhotoCard(
            title = drink.name,
            imageRes = drawableByName(context, drink.imageName, R.drawable.meal_plate),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            height = 180.dp,
            labelSize = 16.sp
        )

        // ---------- which drink ----------
        SectionCard(Modifier.padding(top = 14.dp)) {
            Text("Drink", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
            Box(Modifier.padding(top = 10.dp)) {
                Surface(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    border = BorderStroke(1.5.dp, FoodGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(drink.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = TextDark)
                        Icon(Icons.Filled.ArrowDropDown, "Change drink", tint = FoodGreen)
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = White
                ) {
                    Drinks.all.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.name,
                                    fontWeight = if (option.id == drink.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                expanded = false
                                drinkId = option.id
                            }
                        )
                    }
                }
            }
        }

        // ---------- how many glasses ----------
        SectionCard(Modifier.padding(top = 14.dp)) {
            Text("How many glasses?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlossyIconButton(Icons.Filled.Remove, "Fewer glasses", { if (cups > 1) cups-- }, size = 48.dp)
                Text(
                    "$cups",
                    modifier = Modifier.width(90.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontSize = 40.sp, color = TextDark
                )
                GlossyIconButton(Icons.Filled.Add, "More glasses", { if (cups < MAX_CUPS) cups++ }, size = 48.dp)
            }
            val litres = cups * Drinks.CUP_ML / 1000f
            SmallLabel(
                "${if (cups == 1) "glass" else "glasses"} · ${cups * Drinks.CUP_ML} ml (%.2f L)".format(litres),
                Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }

        errorText?.let {
            Text(
                it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp)
            )
        }

        PrimaryButton(
            text = if (saving) "Saving..." else "Add to my diary",
            enabled = !saving,
            modifier = Modifier.padding(top = 20.dp, bottom = 40.dp),
            onClick = {
                scope.launch {
                    saving = true
                    errorText = null
                    try {
                        withTimeout(10_000) { repository.logDrink(drink.id, cups) }
                        onSaved()
                    } catch (e: TimeoutCancellationException) {
                        errorText = "Saving is taking too long. Check your connection and try again."
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not save drink", e)
                        errorText = "Couldn't save your drink. Please try again."
                    } finally {
                        saving = false
                    }
                }
            }
        )
    }
}