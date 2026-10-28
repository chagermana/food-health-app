package com.foodhealth.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foodhealth.app.ui.MainScreen
import com.foodhealth.app.ui.theme.FoodHealthAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            FoodHealthAppTheme {
                //the navigation controller remembers which screen we are on
                val nav = rememberNavController()
                //if firebase already has a signed in user, skip login and openhome
                val start = if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"

                //surface paints the themes background and sets a matching text color
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                NavHost(navController = nav, startDestination = start) {
                    //login screen
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                nav.navigate("home") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            },
                            onGoToSignUp = {nav.navigate("signup")}
                        )
                    }
                    //sign up screen
                        composable("signup") {
                            SignUpScreen(
                                onSignUpSuccess = {
                                    nav.navigate("home") {
                                        popUpTo("login") {
                                            inclusive = true
                                        }
                                    }
                                },
                                onBackToLogin = { nav.popBackStack() }
                            )
                        }
                    //home screen hosting bottom bar nav and screens flow
                        composable("home") {
                            MainScreen (
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    nav.navigate("login"){
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

