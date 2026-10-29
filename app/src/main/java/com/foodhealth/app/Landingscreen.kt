package com.foodhealth.app

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Lime
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.TextDark

// ====================================================================
// LandingScreen.kt : first page after the splash. Full-screen food photo,
// floating AI labels, and a glossy "Get started" pill that leads to login.
//
// TO CHANGE THE PHOTO: add e.g. landing_bg.jpg to res/drawable and change
// the line below to  R.drawable.landing_bg  (lowercase + underscores only).
// ====================================================================
private val LandingImage = R.drawable.meal_plate

@Composable
fun LandingScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    // gentle up/down float for the AI tags
    val transition = rememberInfiniteTransition(label = "float")
    val floatY by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "y"
    )

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(LandingImage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // dark fade top and bottom so the words stay readable
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0xAA000000),
                    0.35f to Color(0x22000000),
                    0.65f to Color(0x44000000),
                    1f to Color(0xF0000000)
                )
            )
        )

        // top row: brand pill + AI chip
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.clip(CircleShape).background(Color(0x55FFFFFF))
                    .border(1.dp, Color(0x88FFFFFF), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) { append("Dishi") }
                        withStyle(SpanStyle(color = FoodOrange)) { append("Poa") }
                    },
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(CircleShape).background(Lime).padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = DeepGreen, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("AI powered", color = DeepGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // headline
        Column(
            Modifier.align(Alignment.TopStart).statusBarsPadding()
                .padding(start = 24.dp, top = 90.dp, end = 24.dp)
        ) {
            Text("AI Food", color = Color.White, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold, fontSize = 50.sp, lineHeight = 54.sp)
            Text("Insights at your", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)
            Text("Fingertips", color = FoodOrange, fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 44.sp)
        }

        // floating AI labels (positions are relative to the screen)
        FloatingTag("Ugali", "~330 kcal",
            Modifier.align(Alignment.CenterStart).offset(x = 24.dp, y = (40 + floatY).dp))
        FloatingTag("Sukuma wiki", "Rich in iron",
            Modifier.align(Alignment.CenterEnd).offset(x = (-24).dp, y = (-20 - floatY).dp))
        FloatingTag("Beef stew", "28g protein",
            Modifier.align(Alignment.Center).offset(x = 20.dp, y = (120 + floatY).dp))

        // bottom: copy + glossy get-started pill
        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Snap or pick your meal and see calories, macros and simple advice, made for Kenyan food.",
                color = Color(0xDDFFFFFF), fontSize = 14.sp, lineHeight = 20.sp
            )
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth().height(64.dp).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xE6FFFFFF), Color(0xCCFFE3C4))))
                    .border(1.dp, Color.White, CircleShape)
                    .clickable(onClick = onGetStarted)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(FoodOrange, Color(0xFFD98A1F)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Get started", tint = Color.White)
                }
                Text(
                    "Let's analyse your food today",
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = TextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
                Icon(Icons.Filled.AutoAwesome, null, tint = FoodGreen, modifier = Modifier.padding(end = 14.dp))
            }
            TextButton(onClick = onLogin) {
                Text("Already have an account? Log in", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// A small frosted label with a green dot, like the "Cabbage / Green Onion" tags.
@Composable
private fun FloatingTag(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color(0xE6FFFFFF))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(FoodGreen))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
            Text(subtitle, fontSize = 10.sp, color = Muted)
        }
    }
}