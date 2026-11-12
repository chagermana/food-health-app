package com.foodhealth.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.LogoImage
import com.foodhealth.app.R
import com.foodhealth.app.authFieldColors
import com.foodhealth.app.data.Meal
import com.foodhealth.app.data.MealCategory
import com.foodhealth.app.ui.components.GlossyIconButton
import com.foodhealth.app.ui.components.InfoMenuButton
import com.foodhealth.app.ui.components.MealNutriCard
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Lime
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.ui.theme.PeachDeep
import com.foodhealth.app.ui.theme.TextDark
import com.google.firebase.auth.FirebaseAuth

private val FullWidth: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

@Composable
fun HomeScreen(
    meals: List<Meal>,
    isScanning: Boolean,
    onScanClick: () -> Unit,
    onMealClick: (Meal) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<MealCategory?>(null) }   // null = "All"

    val showMeals = remember(query, category, meals) {
        meals.filter { meal ->
            (category == null || meal.category == category) &&
                    (query.isBlank() || meal.name.contains(query.trim(), true))
        }
    }
    val userName = remember {
        FirebaseAuth.getInstance().currentUser?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: "there"
    }

    // warm peach fading into cream, like the reference
    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(PeachDeep, Peach, Cream, Cream)))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = FullWidth) { HomeHeader(userName) }

            item(span = FullWidth) {
                Text(
                    buildAnnotatedString {
                        append("What are you\n")
                        withStyle(SpanStyle(color = FoodOrange)) { append("eating") }
                        append(" today?")
                    },
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontSize = 32.sp, lineHeight = 38.sp, color = TextDark
                )
            }

            item(span = FullWidth) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search a meal, e.g. ugali") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = authFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    GlossyIconButton(Icons.Filled.Tune, "Filters", onClick = { /* later */ }, size = 56.dp)
                }
            }

            item(span = FullWidth) { ScanHero(isScanning, onScanClick) }

            // small menu chips: All / Carbs / Proteins / Veggies / ...
            item(span = FullWidth) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { CategoryChip("All", category == null) { category = null } }
                    items(MealCategory.entries.toList()) { c ->
                        CategoryChip(c.label, category == c) { category = c }
                    }
                }
            }

            item(span = FullWidth) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Recommended for you",
                        modifier = Modifier.weight(1f),
                        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = TextDark
                    )
                    Text("See all", fontSize = 12.sp, color = FoodOrange, fontWeight = FontWeight.SemiBold)
                }
            }

            if (showMeals.isEmpty()) {
                item(span = FullWidth) {
                    Text("No meal found here yet. Try another name or category.", fontSize = 13.sp, color = Muted)
                }
            }
            items(showMeals, key = { it.id }) { meal ->
                MealNutriCard(meal = meal, onClick = { onMealClick(meal) })
            }
        }
    }
}

// Logo + greeting on the left, glossy three-dots menu on the right.
@Composable
private fun HomeHeader(userName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp)
                .shadow(8.dp, CircleShape, ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
                .clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center
        ) { LogoImage(size = 34.dp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp))
            {
                Icon(imageVector = Icons.Outlined.WbSunny, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp) )
                Text("Good Day", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif, letterSpacing = 0.2.sp, color = TextDark)
            }
            Text("Eat well, feel poa", fontSize = 12.sp, color = Muted)
        }
        InfoMenuButton(
            infoTitle = "Home",
            infoText = "Snap a photo of your plate or pick a meal below. Use the chips to filter by food type, " +
                    "then answer a few quick questions and get your nutrition analysis."
        )
    }
}

// Big "Snap your plate" card with AI badge.
@Composable
private fun ScanHero(isScanning: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(12.dp, shape, ambientColor = Color(0x22000000), spotColor = Color(0x44000000))
            .clip(shape)
            .clickable(enabled = !isScanning, onClick = onClick)
    ) {
        Image(
            painter = painterResource(R.drawable.meal_plate),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xE60B3B2C), Color(0x990B3B2C), Color(0x11000000)))
            )
        )
        Column(Modifier.align(Alignment.CenterStart).padding(20.dp)) {
            Row(
                Modifier.clip(CircleShape).background(Lime).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = DeepGreen, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("AI scan", color = DeepGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (isScanning) "Looking at your meal..." else "Snap your plate",
                color = Color.White, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold, fontSize = 22.sp
            )
            Text("Log any meal in 2 seconds", color = Color(0xCCFFFFFF), fontSize = 12.sp)
        }
        Box(
            Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(62.dp)
                .clip(CircleShape).background(Lime),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                CircularProgressIndicator(color = DeepGreen, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            } else {
                Icon(Icons.Outlined.CameraAlt, "Camera", tint = DeepGreen, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = CircleShape
    Box(
        Modifier
            .shadow(if (selected) 6.dp else 3.dp, shape, ambientColor = Color(0x15000000), spotColor = Color(0x22000000))
            .clip(shape)
            .background(
                if (selected) Brush.verticalGradient(listOf(FoodGreen, DeepGreen))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F4EC)))
            )
            .border(1.dp, if (selected) FoodGreen else Color.White, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextDark, maxLines = 1
        )
    }
}