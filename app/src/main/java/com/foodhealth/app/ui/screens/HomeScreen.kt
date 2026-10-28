package com.foodhealth.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CameraAlt
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.LogoImage
import com.foodhealth.app.R
import com.foodhealth.app.authFieldColors
import com.foodhealth.app.data.Meal
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.theme.Muted
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
    // What user types in search box
    var query by remember { mutableStateOf("") }
    // Meals whose name contains the text (ignoting caps)
    val showMeals = remember(query, meals) {
        if (query.isBlank()) meals
        else meals.filter { it.name.contains(query.trim(), true) }
    }
    //First name for greeting, i.e part of the email before @
    val userName = remember {
        FirebaseAuth.getInstance().currentUser?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: "there"
    }

    // One scrolling grid for the whole page
    // Each meal takes one column each
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = FullWidth) { HomeHeader(userName) }
        item(span = FullWidth) { ScanTitle(isScanning, onScanClick) }
        item(span = FullWidth) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Or search a meal, e.g. ugali") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = authFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(span = FullWidth) {
            Text(
                "Popular Kenyan meals",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextDark,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        if (showMeals.isEmpty()) {
            item(span = FullWidth) {
                Text("No meal found. Try another name.", fontSize = 13.sp, color = Muted)
            }
        }
        items(showMeals, key = { it.id }) { meal ->
            MealPhotoCard(
                title = meal.name,
                imageRes = meal.imageRes,
                onClick = { onMealClick(meal) }
            )
        }
    }
}

// Logo on the left, greeting and question on the top-right side of the screen
@Composable
private fun HomeHeader(userName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LogoImage(size = 52.dp) // reused from AuthComponents.kt
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Habari, $userName \uD83D\uDC4B", fontSize = 13.sp, color = Muted)
            Text(
                "What are you eating today?",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextDark
            )
        }
    }
}

// Photo-scan area
// Place to "snap your meal"
@Composable
private fun ScanTitle(isScanning: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = !isScanning, onClick = onClick)
    ) {
        Image(
            painter = painterResource(R.drawable.meal_plate),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // dark layer for white text to readable
        Box(Modifier.fillMaxSize().drawBehind { drawRect(Color(0x88000000)) })
        // dashed frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color(0x99FFFFFF),
                        cornerRadius = CornerRadius(18.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isScanning) {
                    // show while  modiel is working
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Looking at your meal...", color = Color.White, fontSize = 13.sp)
                } else {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Tap to snap your meal", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}