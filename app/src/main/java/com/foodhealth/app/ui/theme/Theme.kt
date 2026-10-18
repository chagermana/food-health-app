package com.foodhealth.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Maps our brand colours to Material's colour roles.
// Material components (buttons, text fields, text) read their colours from here.
private val AppColors = lightColorScheme(
    primary = FoodGreen,
    onPrimary = White,             // text drawn on top of green
    secondary = FoodOrange,
    onSecondary = DarkBrown,       // text drawn on top of saffron
    background = Cream,
    onBackground = TextDark,
    surface = White,
    onSurface = TextDark,
    onSurfaceVariant = Muted,      // labels and small text
    outline = Line                 // text field borders
)

// Light theme only. We do not use dark mode or "dynamic colour" (the phone's
// wallpaper colours), so the colours look the same on every phone.
@Composable
fun FoodHealthAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = Typography,   // comes from Type.kt
        content = content
    )
}