package com.foodhealth.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.White
import kotlinx.coroutines.launch

// =====================================================================
// MainScreen.kt : the layout you see AFTER logging in (WhatsApp/Instagram
// style): a screen area on top and a bottom bar with 4 tabs.
//
//   Scaffold          = the frame (content area + bottom bar + snackbar)
//   NavHost           = the area that swaps screens when you navigate
//   NavigationBar     = the bottom bar with the 4 icons
//
// This file is the ONLY place that connects the screens to each other, to
// the ML recognizer and (later) to Firestore. Screens stay "dumb".
// =====================================================================
@Composable
fun MainScreen(onLogout: () -> Unit) {
    // Second NavController, only for the screens inside the bottom-bar layout.
    // (Manvin's controller in MainActivity handles login -> signup -> home.)
    val navController = rememberNavController()

    val flow = remember { MealFlowState() }                       // the meal being worked on
    // ===== ML PLUG POINT =================================================
    // Today: the fake. When the model is ready, replace this ONE line with
    //   TfLiteMealRecognizer(LocalContext.current.applicationContext)
    // (see ML_INTEGRATION_GUIDE.md)
    val recognizer: MealRecognizer = remember { FakeMealRecognizer() }
    // =====================================================================
    var isScanning by remember { mutableStateOf(false) }          // true while the model works
    val snackbarHostState = remember { SnackbarHostState() }      // small messages at the bottom
    val scope = rememberCoroutineScope()                          // lets us start background work

    // Which screen are we on right now? Used to highlight the tab and to hide
    // the bottom bar on Portion and Error.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = BottomTab.entries.any { it.route == currentRoute }

    // Switch tab without piling up screens (the usual bottom-bar behaviour).
    fun goToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(currentRoute = currentRoute, onTabClick = { goToTab(it) })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)   // keeps content clear of the bars
        ) {
            // ---------- Tab 1: Home ----------
            composable(Routes.HOME) {
                HomeScreen(
                    meals = SampleMeals.all,
                    isScanning = isScanning,
                    onMealClick = { meal ->
                        flow.startMeal(meal)
                        navController.navigate(Routes.PORTION)
                    },
                    onScanClick = {
                        // Camera tile tapped. When CameraX exists (week 3) this will
                        // first open the camera screen and pass the photo in.
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
                                navController.navigate(Routes.ERROR)   // model crashed -> error page
                            } finally {
                                isScanning = false
                            }
                        }
                    }
                )
            }

            // ---------- Portion questions (not a tab) ----------
            composable(Routes.PORTION) {
                PortionScreen(
                    flow = flow,
                    onBack = { navController.popBackStack() },
                    onGoHome = { goToTab(Routes.HOME) },
                    onAnalyse = { navController.navigate(Routes.ANALYSIS) }
                )
            }

            // ---------- Tab 2: Analysis ----------
            composable(Routes.ANALYSIS) {
                AnalysisScreen(
                    flow = flow,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Routes.RATE) },
                    onGoHome = { goToTab(Routes.HOME) }
                )
            }

            // ---------- Tab 3: Rate ----------
            composable(Routes.RATE) {
                RateScreen(
                    flow = flow,
                    onBack = { navController.popBackStack() },
                    onGoHome = { goToTab(Routes.HOME) },
                    onSave = {
                        // WEEK 4: save flow.meal, flow.result and flow.rating to
                        // Firestore here, before navigating.
                        scope.launch { snackbarHostState.showSnackbar("Meal added to your diary") }
                        goToTab(Routes.PROFILE)
                    }
                )
            }

            // ---------- Tab 4: Profile ----------
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = onLogout,
                    onPreviewError = { navController.navigate(Routes.ERROR) }
                )
            }

            // ---------- Error page (not a tab) ----------
            composable(Routes.ERROR) {
                ErrorScreen(
                    onRetry = { navController.popBackStack() },
                    onGoHome = { goToTab(Routes.HOME) }
                )
            }
        }
    }
}

// The bottom bar. One item per BottomTab (see navigation/AppNavigation.kt).
@Composable
private fun AppBottomBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    NavigationBar(containerColor = White, tonalElevation = 0.dp) {
        BottomTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabClick(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FoodGreen,
                    selectedTextColor = FoodGreen,
                    indicatorColor = SoftGreen,       // the pill behind the active icon
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
            )
        }
    }
}
