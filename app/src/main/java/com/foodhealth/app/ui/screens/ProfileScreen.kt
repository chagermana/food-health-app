package com.foodhealth.app.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.R
import com.foodhealth.app.data.DrinkRepository
import com.foodhealth.app.data.MealRepository
import com.foodhealth.app.data.NutritionTargets
import com.foodhealth.app.data.ProfileStatsBuilder
import com.foodhealth.app.ui.components.CalorieRing
import com.foodhealth.app.ui.components.InfoMenuButton
import com.foodhealth.app.ui.components.LabeledBar
import com.foodhealth.app.ui.components.MealDiarySection
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.MenuAction
import com.foodhealth.app.ui.theme.CarbColor
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FatColor
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodGreenLight
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Lime
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.ProteinColor
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.WaterBlue
import com.foodhealth.app.util.DateKeys
import com.foodhealth.app.util.drawableByName
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "ProfileScreen"
private const val WINDOW_DAYS = 60            // history loaded (also the longest streak shown)
private const val WATER_GOAL_L = 2.5
private const val FRUIT_VEG_GOAL_G = 400.0    // WHO: at least 400 g of fruit and veg a day

// Daily goals = 3 meals x the per-meal targets already in Meal.kt.
private val DailyKcal = NutritionTargets.MEAL_KCAL * 3.0
private val DailyProtein = NutritionTargets.PROTEIN_G * 3.0
private val DailyCarbs = NutritionTargets.CARBS_G * 3.0
private val DailyFat = NutritionTargets.FAT_G * 3.0

// =====================================================================
// ProfileScreen.kt : dashboard built from the user's REAL data:
//   meals  -> users/{uid}/meals      drinks -> users/{uid}/drinks
// =====================================================================
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onPreviewError: () -> Unit,
    onLogDrink: (String) -> Unit
) {
    val context = LocalContext.current
    val email = remember { FirebaseAuth.getInstance().currentUser?.email ?: "guest@dishipoa" }
    val displayName = remember(email) { email.substringBefore("@").replaceFirstChar { it.uppercase() } }

    val mealRepo = remember { MealRepository() }
    val drinkRepo = remember { DrinkRepository() }
    val keys = remember { DateKeys.lastDays(WINDOW_DAYS).map { it.key } }
    val weekDays = remember { DateKeys.lastDays(7) }

    var stats by remember { mutableStateOf(ProfileStatsBuilder.build(emptyList(), emptyList(), keys)) }
    var failed by remember { mutableStateOf(false) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var started by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableIntStateOf(6) }    // index into the week, 6 = today

    LaunchedEffect(Unit) { started = true }

    LaunchedEffect(reloadTick) {
        failed = false
        try {
            withTimeout(15_000) {
                val meals = mealRepo.mealsBetween(keys.first(), keys.last())
                val drinks = drinkRepo.drinksBetween(keys[keys.size - 7], keys.last())
                stats = ProfileStatsBuilder.build(meals, drinks, keys)
            }
        } catch (e: TimeoutCancellationException) {
            failed = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Could not load profile stats", e)
            failed = true
        }
    }

    val today = stats.today
    val week = stats.week

    val ring by animateFloatAsState(
        if (started) (today.kcal / DailyKcal).toFloat().coerceIn(0f, 1f) else 0f, tween(900), label = "ring"
    )
    val pBar by animateFloatAsState(
        if (started) (today.proteinG / DailyProtein).toFloat().coerceIn(0f, 1f) else 0f, tween(800), label = "p"
    )
    val cBar by animateFloatAsState(
        if (started) (today.carbsG / DailyCarbs).toFloat().coerceIn(0f, 1f) else 0f, tween(800), label = "c"
    )
    val fBar by animateFloatAsState(
        if (started) (today.fatG / DailyFat).toFloat().coerceIn(0f, 1f) else 0f, tween(800), label = "f"
    )

    val cardShadow = Color(0x22000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- header: avatar + greeting + three dots ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape, ambientColor = cardShadow, spotColor = cardShadow)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(R.drawable.person_1),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    displayName,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextDark
                )
                Text(email, fontSize = 11.sp, color = Muted)
            }
            InfoMenuButton(
                infoTitle = "Profile",
                infoText = "Your dashboard: today's calories and macros, water, fruit and veg, your weekly trend, " +
                        "logging streak and favourite meals. Everything comes from the meals and drinks you have logged.",
                extraActions = listOf(
                    // TODO: remove "Preview error screen" before the final APK
                    MenuAction("Preview error screen", Icons.Outlined.BugReport, onPreviewError),
                    MenuAction("Log out", Icons.AutoMirrored.Filled.Logout, onLogout)
                )
            )
        }

        if (failed) {
            Column {
                Text("Couldn't load your latest data. Check your connection.", fontSize = 12.sp, color = Muted)
                TextButton(onClick = { reloadTick++ }) { Text("Try again", color = FoodGreen) }
            }
        }

        // ---------- hero: today's ring + macro level bars ----------
        val heroShape = RoundedCornerShape(30.dp)
        Column(
            Modifier.fillMaxWidth()
                .shadow(14.dp, heroShape, ambientColor = cardShadow, spotColor = Color(0x55000000))
                .clip(heroShape)
                .background(Brush.linearGradient(listOf(DeepGreen, FoodGreen)))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CalorieRing(
                    progress = ring,
                    centerText = today.kcal.roundToInt().toString(),
                    subText = "kcal today",
                    ringSize = 130.dp, trackColor = Color(0x33FFFFFF), progressColor = Lime,
                    textColor = Color.White, subColor = Color(0xBBFFFFFF)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.clip(CircleShape).background(Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = FoodOrange, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Today", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    val track = Color(0x33FFFFFF)
                    LabeledBar(
                        "Protein", "${today.proteinG.roundToInt()} / ${DailyProtein.roundToInt()} g",
                        pBar, ProteinColor, labelColor = Color.White, trackColor = track
                    )
                    LabeledBar(
                        "Carbs", "${today.carbsG.roundToInt()} / ${DailyCarbs.roundToInt()} g",
                        cBar, CarbColor, labelColor = Color.White, trackColor = track
                    )
                    LabeledBar(
                        "Fat", "${today.fatG.roundToInt()} / ${DailyFat.roundToInt()} g",
                        fBar, FatColor, labelColor = Color.White, trackColor = track
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Protein", "${today.proteinG.roundToInt()} g", Modifier.weight(1f))
                StatTile("Carbs", "${today.carbsG.roundToInt()} g", Modifier.weight(1f))
                StatTile("Fruits & veg", "${today.fruitVegG.roundToInt()} g", Modifier.weight(1f))
                StatTile("Water", "%.2f L".format(today.waterL), Modifier.weight(1f))
            }
        }

        // ----------- meal diary (real data from Firestore) -------------
        MealDiarySection()

        // ---------- water and fruit & veg ----------
        val cardShape = RoundedCornerShape(26.dp)
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Water
            Column(
                Modifier.weight(1f).fillMaxSize()
                    .shadow(8.dp, cardShape, ambientColor = cardShadow, spotColor = cardShadow)
                    .clip(cardShape).background(Color.White).padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WaterDrop, null, tint = WaterBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Water", fontSize = 13.sp, color = Muted, modifier = Modifier.weight(1f))
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(FoodGreen)
                            .clickable { onLogDrink("water") },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Add, "Log water", tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("%.2f".format(today.waterL), fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold, fontSize = 26.sp, color = TextDark)
                    Text(" / ${WATER_GOAL_L} L", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                SegmentBar(filled = (today.waterL * 1000 / 250).toInt().coerceIn(0, 10), color = WaterBlue)
            }
            // Fruits & veg
            Column(
                Modifier.weight(1f).fillMaxSize()
                    .shadow(8.dp, cardShape, ambientColor = cardShadow, spotColor = cardShadow)
                    .clip(cardShape).background(Color.White).padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Park, null, tint = ProteinColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Fruits & veg", fontSize = 13.sp, color = Muted, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${today.fruitVegG.roundToInt()}", fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold, fontSize = 26.sp, color = TextDark)
                    Text(" / ${FRUIT_VEG_GOAL_G.roundToInt()} g", fontSize = 12.sp, color = Muted,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                SegmentBar(filled = (today.fruitVegG / (FRUIT_VEG_GOAL_G / 10)).toInt().coerceIn(0, 10), color = ProteinColor)
            }
        }

        // ---------- streak ----------
        Row(
            Modifier.fillMaxWidth()
                .shadow(8.dp, cardShape, ambientColor = cardShadow, spotColor = cardShadow)
                .clip(cardShape)
                .background(Brush.verticalGradient(listOf(Lime, Color(0xFFBFE04F))))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.LocalFireDepartment, null, tint = DeepGreen, modifier = Modifier.size(34.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    if (stats.streak >= WINDOW_DAYS) "$WINDOW_DAYS+ days" else "${stats.streak}-day",
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontSize = 26.sp, color = DeepGreen
                )
                Text("logging streak", fontSize = 12.sp, color = DeepGreen)
            }
        }

        // ---------- weekly bars (tap a day) ----------
        GlossCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This week", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    color = TextDark, modifier = Modifier.weight(1f))
                Text(
                    "${weekDays[selectedDay].weekday.take(3)} · ${week[selectedDay].kcal.roundToInt()} kcal",
                    fontSize = 12.sp, color = FoodOrange, fontWeight = FontWeight.Bold
                )
            }
            val scale = max(2000.0, week.maxOf { it.kcal })
            Row(
                Modifier.fillMaxWidth().height(120.dp).padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                week.forEachIndexed { index, day ->
                    Column(
                        Modifier.weight(1f).clickable { selectedDay = index },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            Modifier.fillMaxWidth()
                                .height(max(4f, (day.kcal / scale * 90).toFloat()).dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (index == selectedDay) FoodOrange else FoodGreenLight.copy(alpha = 0.75f))
                        )
                        Text(weekDays[index].weekday.take(1), fontSize = 10.sp, color = Muted,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        // ---------- donut: average macro split of the week ----------
        val split = listOf(
            Triple("Protein", stats.macroSplit[0], ProteinColor),
            Triple("Carbs", stats.macroSplit[1], CarbColor),
            Triple("Fat", stats.macroSplit[2], FatColor)
        )
        val donutAnim by animateFloatAsState(if (started) 1f else 0f, tween(1000), label = "donut")
        GlossCard {
            Text("Average macro split", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(Modifier.size(120.dp).padding(10.dp)) {
                    val stroke = Stroke(width = 18.dp.toPx())
                    drawArc(Line, 0f, 360f, false, style = stroke)   // empty track
                    var start = -90f
                    split.forEach { (_, frac, color) ->
                        val sweep = 360f * frac * donutAnim
                        drawArc(color, start, (sweep - 3f).coerceAtLeast(0f), false, style = stroke)
                        start += sweep
                    }
                }
                Spacer(Modifier.width(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    split.forEach { (name, frac, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                            Spacer(Modifier.width(8.dp))
                            Text("$name  ${(frac * 100).roundToInt()}%", fontSize = 13.sp, color = TextDark)
                        }
                    }
                }
            }
        }

        // ---------- top 3 meals (from how often each food was logged) ----------
        Text("Top 3 meals", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = 16.sp, color = TextDark)
        if (stats.topFoods.isEmpty()) {
            Text("Scan a few meals and your favourites will show up here.", fontSize = 13.sp, color = Muted)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.topFoods.forEach { (food, _) ->
                    MealPhotoCard(
                        title = food.displayName,
                        imageRes = drawableByName(context, "food_${food.label}", R.drawable.meal_plate),
                        modifier = Modifier.weight(1f),
                        height = 96.dp,
                        labelSize = 10.sp
                    )
                }
                repeat(3 - stats.topFoods.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        Button(
            onClick = onLogout,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 120.dp).height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log out", fontWeight = FontWeight.Bold)
        }
    }
}

// Ten little blocks that fill up from the left.
@Composable
private fun SegmentBar(filled: Int, color: Color) {
    Row(Modifier.fillMaxWidth().height(26.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 0 until 10) {
            Box(
                Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(4.dp))
                    .background(if (i < filled) color else Line)
            )
        }
    }
}

// Rounded white iOS-style card with a soft shadow.
@Composable
private fun GlossCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        Modifier.fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFCF8F0))))
            .padding(16.dp)
    ) { content() }
}

// Small translucent tile on the dark hero card.
@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color(0x22FFFFFF)).padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 10.sp, color = Color(0xBBFFFFFF), maxLines = 1)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
    }
}