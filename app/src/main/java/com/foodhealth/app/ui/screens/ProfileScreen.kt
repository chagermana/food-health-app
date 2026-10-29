package com.foodhealth.app.ui.screens

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
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.SampleMeals
import com.foodhealth.app.ui.components.CalorieRing
import com.foodhealth.app.ui.components.InfoMenuButton
import com.foodhealth.app.ui.components.LabeledBar
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
import com.google.firebase.auth.FirebaseAuth

// =====================================================================
// ProfileScreen.kt : "Morning Ahmed" style dashboard. All numbers are
// STATIC placeholders (TODO: real data from Firestore in week 4).
// Interactive bits: water segments, weekly bars (tap a day), donut chart.
// =====================================================================
@Composable
fun ProfileScreen(onLogout: () -> Unit, onPreviewError: () -> Unit) {
    val email = remember { FirebaseAuth.getInstance().currentUser?.email ?: "guest@dishipoa" }
    val displayName = remember(email) { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
    val initials = remember(displayName) { displayName.take(2).uppercase() }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    var glasses by remember { mutableIntStateOf(5) }        // 1 glass = 0.25 L, max 10 = 2.5 L
    var selectedDay by remember { mutableIntStateOf(6) }    // index into the week

    val ring by animateFloatAsState(if (started) 0.37f else 0f, tween(900), label = "ring")
    val pBar by animateFloatAsState(if (started) 0.69f else 0f, tween(800), label = "p")
    val cBar by animateFloatAsState(if (started) 0.92f else 0f, tween(800), label = "c")
    val fBar by animateFloatAsState(if (started) 0.61f else 0f, tween(800), label = "f")

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
                            painter = painterResource(com.foodhealth.app.R.drawable.person_1),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp))
                {
                    Icon(imageVector = Icons.Outlined.WbSunny, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp) )
                    Text("What's good", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif, letterSpacing = 0.2.sp, color = TextDark)
                }
                Text(email, fontSize = 11.sp, color = Muted)   // TODO: add the age + goal from Firestore once we start to store user data
            }
            InfoMenuButton(
                infoTitle = "Profile",
                infoText = "Your dashboard: today's calories and macros, water, weekly trend and your favourite meals. " +
                        "These numbers are examples until meal saving is connected.",
                extraActions = listOf(
                    // TODO: remove "Preview error screen" before the final APK
                    MenuAction("Preview error screen", Icons.Outlined.BugReport, onPreviewError),
                    MenuAction("Log out", Icons.AutoMirrored.Filled.Logout, onLogout)
                )
            )
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
                    progress = ring, centerText = "610", subText = "kcal left",
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
                    LabeledBar("Protein", "96 / 140 g", pBar, ProteinColor, labelColor = Color.White, trackColor = track)
                    LabeledBar("Carbs", "120 / 130 g", cBar, CarbColor, labelColor = Color.White, trackColor = track)
                    LabeledBar("Fat", "38 / 62 g", fBar, FatColor, labelColor = Color.White, trackColor = track)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealTimeTile("Breakfast", "420", Modifier.weight(1f))
                MealTimeTile("Lunch", "610", Modifier.weight(1f))
                MealTimeTile("Snack", "210", Modifier.weight(1f))
                MealTimeTile("Dinner", "—", Modifier.weight(1f))
            }
        }

        // ---------- water (tap segments / +) and streak ----------
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val cardShape = RoundedCornerShape(26.dp)
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
                            .clickable { if (glasses < 10) glasses++ },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Add, "Add a glass", tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${"%.2f".format(glasses * 0.25f)}", fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold, fontSize = 26.sp, color = TextDark)
                    Text(" / 2.5 L", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth().height(26.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (i in 0 until 10) {
                        Box(
                            Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(4.dp))
                                .background(if (i < glasses) WaterBlue else Line)
                                .clickable { glasses = i + 1 }
                        )
                    }
                }
            }
            Column(
                Modifier.weight(1f).fillMaxSize()
                    .shadow(8.dp, cardShape, ambientColor = cardShadow, spotColor = cardShadow)
                    .clip(cardShape)
                    .background(Brush.verticalGradient(listOf(Lime, Color(0xFFBFE04F))))
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = DeepGreen, modifier = Modifier.size(28.dp))
                Column {
                    Text("12-day", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                        fontSize = 26.sp, color = DeepGreen)
                    Text("logging streak", fontSize = 12.sp, color = DeepGreen)
                }
            }
        }

        // ---------- weekly bars (tap a day) ----------
        val weekKcal = listOf(1450, 1820, 1200, 1900, 1650, 1500, 1710)   // TODO: real daily totals
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        GlossCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This week", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    color = TextDark, modifier = Modifier.weight(1f))
                Text("${dayNames[selectedDay]} · ${weekKcal[selectedDay]} kcal", fontSize = 12.sp,
                    color = FoodOrange, fontWeight = FontWeight.Bold)
            }
            Row(
                Modifier.fillMaxWidth().height(120.dp).padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weekKcal.forEachIndexed { index, value ->
                    Column(
                        Modifier.weight(1f).clickable { selectedDay = index },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            Modifier.fillMaxWidth()
                                .height((value / 2000f * 90f).dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (index == selectedDay) FoodOrange else FoodGreenLight.copy(alpha = 0.75f))
                        )
                        Text(dayNames[index].take(1), fontSize = 10.sp, color = Muted,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        // ---------- donut: average macro split ----------
        val split = listOf(
            Triple("Protein", 0.22f, ProteinColor),
            Triple("Carbs", 0.52f, CarbColor),
            Triple("Fat", 0.26f, FatColor)
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
                            Text("$name  ${(frac * 100).toInt()}%", fontSize = 13.sp, color = TextDark)
                        }
                    }
                }
            }
        }

        // ---------- top 3 meals ----------
        Text("Top 3 meals", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = 16.sp, color = TextDark)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SampleMeals.all.take(3).forEach { meal ->
                MealPhotoCard(
                    title = meal.name.substringBefore(" "),
                    imageRes = meal.imageRes,
                    modifier = Modifier.weight(1f),
                    height = 96.dp,
                    labelSize = 10.sp
                )
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
private fun MealTimeTile(label: String, kcal: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color(0x22FFFFFF)).padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 10.sp, color = Color(0xBBFFFFFF), maxLines = 1)
        Text(kcal, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}