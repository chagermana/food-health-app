package com.foodhealth.app.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foodhealth.app.data.MealRepository
import com.foodhealth.app.data.SampleMeals
import com.foodhealth.app.data.ScanSource
import com.foodhealth.app.ml.FoodClassifier
import com.foodhealth.app.ml.ImageLoader
import com.foodhealth.app.navigation.BottomTab
import com.foodhealth.app.navigation.Routes
import com.foodhealth.app.ui.components.ScanSourceDialog
import com.foodhealth.app.ui.screens.AnalysisScreen
import com.foodhealth.app.ui.screens.ErrorScreen
import com.foodhealth.app.ui.screens.HomeScreen
import com.foodhealth.app.ui.screens.ProfileScreen
import com.foodhealth.app.ui.screens.RateScreen
import com.foodhealth.app.ui.screens.ScanResultScreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.White
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File

private const val TAG = "MainScreen"

// =====================================================================
// MainScreen.kt : the layout AFTER logging in: the screens fill the whole
// area and a translucent, floating, round-edged bar sits ON TOP of them.
//
// Scan flow: Home -> (camera | gallery) -> ML model -> Scan result ->
//            Analysis -> Rate -> Profile (diary)
// =====================================================================
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val flow = remember { MealFlowState() }
    val repository = remember { MealRepository() }

    // ===== ML PLUG POINT: the real on-device food model =====
    // If the model or labels.txt fail to load, classifier is null and the
    // scan buttons show a friendly message instead of crashing the app.
    val classifier = remember {
        runCatching { FoodClassifier(context) }
            .onFailure { Log.e(TAG, "Could not start the food classifier", it) }
            .getOrNull()
    }
    DisposableEffect(classifier) { onDispose { classifier?.close() } }
    // ========================================================

    var isScanning by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
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

    // Shows a message without waiting for it to disappear.
    fun toast(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // photo -> upright bitmap -> ML model -> Scan result screen
    fun analyse(uri: Uri, source: ScanSource) {
        scope.launch {
            isScanning = true
            try {
                val engine = classifier
                if (engine == null) {
                    toast("The food scanner could not start. Please restart the app.")
                    return@launch
                }
                val bitmap = ImageLoader.load(context, uri)
                if (bitmap == null) {
                    toast("We couldn't read that photo. Try another one.")
                    return@launch
                }
                val predictions = engine.classify(bitmap)
                flow.startScan(bitmap, source, predictions)
                navController.navigate(Routes.SCAN_RESULT)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Scan failed", e)
                navController.navigate(Routes.ERROR)
            } finally {
                isScanning = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) analyse(uri, ScanSource.GALLERY)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val uri = pendingCameraUri
        if (saved && uri != null) analyse(uri, ScanSource.CAMERA)
    }

    fun launchCamera() {
        try {
            val dir = File(context.cacheDir, "photos").apply { mkdirs() }
            val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open the camera", e)
            toast("No camera available on this device.")
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
                            flow.startManual(meal)
                            navController.navigate(Routes.SCAN_RESULT)
                        },
                        onScanClick = { showSourceDialog = true }
                    )
                }

                composable(Routes.SCAN_RESULT) {
                    ScanResultScreen(
                        flow = flow,
                        repository = repository,
                        onBack = { navController.popBackStack() },
                        onGoHome = { goToTab(Routes.HOME) },
                        onProceed = { navController.navigate(Routes.ANALYSIS) }
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
                            scope.launch {
                                try {
                                    if (flow.rating > 0) {
                                        withTimeout(10_000) {
                                            repository.saveRating(flow.mealId, flow.rating)
                                        }
                                    }
                                    toast("Meal added to your diary")
                                    goToTab(Routes.PROFILE)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e(TAG, "Could not save rating", e)
                                    toast("Couldn't save your rating. Please try again.")
                                }
                            }
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

    if (showSourceDialog) {
        ScanSourceDialog(
            onDismiss = { showSourceDialog = false },
            onCamera = {
                showSourceDialog = false
                launchCamera()
            },
            onGallery = {
                showSourceDialog = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

// The floating translucent pill. Active tab = green pill with icon + label,
// other tabs = just the icon. (unchanged from before)
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
                .shadow(
                    10.dp, barShape,
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x22000000)
                )
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.62f),
                            Color(0xFFF6F1E7).copy(alpha = 0.42f)
                        )
                    )
                )
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