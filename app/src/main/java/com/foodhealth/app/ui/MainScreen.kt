package com.foodhealth.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foodhealth.app.data.SampleMeals
import com.foodhealth.app.ml.FakeMealRecognizer
import com.foodhealth.app.ml.MealRecognizer
import com.foodhealth.app.navigation.BottomTab
import com.foodhealth.app.navigation.Routes
import com.foodhealth.app.ui.screens.AnalysisScreen
import com.foodhealth.app.ui.screens.ErrorScreen
import com.foodhealth.app.ui.screens.HomeScreen
import com.foodhealth.app.ui.screens.PortionScreen
import com.foodhealth.app.ui.screens.ProfileScreen
import com.foodhealth.app.ui.screens.RateScreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.White
import kotlinx.coroutines.launch

// =====================================================================
// MainScreen.kt : the layout AFTER logging in: the screens fill the whole
// area and a translucent, floating, round-edged bar sits ON TOP of them.
// =====================================================================
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    val flow = remember { MealFlowState() }
    // ===== ML PLUG POINT: replace this ONE line when the model is ready =====
    val recognizer: MealRecognizer = remember { FakeMealRecognizer() }
    // =======================================================================
    var isScanning by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = BottomTab.entries.any { it.route == currentRoute }

    fun goToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // No bottomBar slot: that slot reserves a solid strip under the bar.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                // top only (status bar). Bottom is left free so content runs UNDER the bar.
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        meals = SampleMeals.all,
                        isScanning = isScanning,
                        onMealClick = { meal ->
                            flow.startMeal(meal)
                            navController.navigate(Routes.PORTION)
                        },
                        onScanClick = {
                            scope.launch {
                                isScanning = true
                                try {
                                    val recognition = recognizer.recognize(null)
                                    val meal = recognition?.let { SampleMeals.byId(it.mealId) }
                                    if (meal != null) {
                                        flow.startMeal(meal)
                                        navController.navigate(Routes.PORTION)
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            "We couldn't recognise that meal. Pick one below."
                                        )
                                    }
                                } catch (e: Exception) {
                                    navController.navigate(Routes.ERROR)
                                } finally {
                                    isScanning = false
                                }
                            }
                        }
                    )
                }

                composable(Routes.PORTION) {
                    PortionScreen(
                        flow = flow,
                        onBack = { navController.popBackStack() },
                        onGoHome = { goToTab(Routes.HOME) },
                        onAnalyse = { navController.navigate(Routes.ANALYSIS) }
                    )
                }

                composable(Routes.ANALYSIS) {
                    AnalysisScreen(
                        flow = flow,
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.navigate(Routes.RATE) },
                        onGoHome = { goToTab(Routes.HOME) }
                    )
                }

                composable(Routes.RATE) {
                    RateScreen(
                        flow = flow,
                        onBack = { navController.popBackStack() },
                        onGoHome = { goToTab(Routes.HOME) },
                        onSave = {
                            // WEEK 4: save flow.meal, flow.result and flow.rating to Firestore here.
                            scope.launch { snackbarHostState.showSnackbar("Meal added to your diary") }
                            goToTab(Routes.PROFILE)
                        }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = onLogout,
                        onPreviewError = { navController.navigate(Routes.ERROR) }
                    )
                }

                composable(Routes.ERROR) {
                    ErrorScreen(
                        onRetry = { navController.popBackStack() },
                        onGoHome = { goToTab(Routes.HOME) }
                    )
                }
            }

            // drawn AFTER the NavHost, so it floats on top of the screen
            if (showBottomBar) {
                FloatingBottomBar(
                    currentRoute = currentRoute,
                    onTabClick = { goToTab(it) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// The floating translucent pill. Active tab = green pill with icon + label,
// other tabs = just the icon. Same icons as before (BottomTab).
// NOTE: top-level function (it was nested inside MainScreen before).
@Composable
private fun FloatingBottomBar(
    currentRoute: String?,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(34.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                // soft shadow; keep it light so it doesn't show as a grey smudge through the glass
                .shadow(
                    10.dp, barShape,
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x22000000)
                )
                .clip(barShape)
                // translucent glass: more see-through = lower alpha
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.62f),
                            Color(0xFFF6F1E7).copy(alpha = 0.42f)
                        )
                    )
                )
                // bright rim, fading downwards, gives the glossy edge
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.25f))
                    ),
                    barShape
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val selected = currentRoute == tab.route
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (selected) FoodGreen else Color.Transparent)
                        .clickable { onTabClick(tab.route) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = if (selected) White else Muted
                    )
                    AnimatedVisibility(
                        visible = selected,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        Text(
                            tab.label, color = White, fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, maxLines = 1,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}