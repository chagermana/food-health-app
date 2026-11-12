package com.foodhealth.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import kotlinx.coroutines.delay

// Splash: logo pops in, fades, then calls onFinished() after ~1.8 s.
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (started) 1f else 0f, tween(700), label = "alpha")
    val scale by animateFloatAsState(if (started) 1f else 0.7f, tween(700), label = "scale")

    LaunchedEffect(Unit) {
        started = true
        delay(1800)
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(FoodGreen, DeepGreen))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.alpha(alpha).scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(120.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) { LogoImage(size = 84.dp) }
            Spacer(Modifier.height(16.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Dishi") }
                    withStyle(SpanStyle(color = FoodOrange)) { append("Poa") }
                },
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp
            )
            Text("Know what's on your plate.", color = Color(0xCCFFFFFF), fontSize = 14.sp)
        }
    }
}