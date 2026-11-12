package com.foodhealth.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foodhealth.app.ui.MainScreen
import com.foodhealth.app.ui.theme.FoodHealthAppTheme
import com.google.firebase.auth.FirebaseAuth

// Flow:  splash -> (signed in? home : landing) -> login <-> signup -> home
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            FoodHealthAppTheme {
                val nav = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = nav, startDestination = "splash") {

                        composable("splash") {
                            SplashScreen(onFinished = {
                                // already signed in? skip landing + login
                                val next = if (FirebaseAuth.getInstance().currentUser != null) "home" else "landing"
                                nav.navigate(next) { popUpTo("splash") { inclusive = true } }
                            })
                        }

                        composable("landing") {
                            LandingScreen(
                                onGetStarted = { nav.navigate("login") },
                                onLogin = { nav.navigate("login") }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    nav.navigate("home") { popUpTo("landing") { inclusive = true } }
                                },
                                onGoToSignUp = { nav.navigate("signup") }
                            )
                        }

                        composable("signup") {
                            SignUpScreen(
                                onSignUpSuccess = {
                                    nav.navigate("home") { popUpTo("landing") { inclusive = true } }
                                },
                                onBackToLogin = { nav.popBackStack() }
                            )
                        }

                        composable("home") {
                            MainScreen(
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    nav.navigate("login") { popUpTo("home") { inclusive = true } }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

