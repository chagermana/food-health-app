package com.foodhealth.app.navigation



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector

// ---- App Navigation.kt - list of screens (routes) and the bottom tabs
// It's the map of the application

object Routes {
    const val HOME = "tab_home"
    const val ANALYSIS = "tab_analysis"
    const val RATE = "tab_route"
    const val PROFILE = "tab_profile"
    const val DRINK = "drink/{drinkId}"
    fun drink(id: String) = "drink/$id"
    const val SCAN_RESULT = "scan_result"  // photo percentages + edit foods (replaced the old PORTION route)
    const val ERROR = "error"

}

// 4 buttons for the bottom bar
// Icons from Google Material icons (fonts. google.com/icons)
enum class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,  // filled when tab is active.
    val unselectedIcon: ImageVector // when tab is not active.
) {
    Home(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    Analysis(Routes.ANALYSIS, "Analysis", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    Rate(Routes.RATE, "Rate", Icons.Filled.Star, Icons.Outlined.StarBorder),
    Profile(Routes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}