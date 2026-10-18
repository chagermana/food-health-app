package com.foodhealth.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.White

// Shared pieces for the login and sign up screens, written once here
// so both screens always look the same.
@Composable
fun LogoImage(size: Dp = 96.dp) {
    Image(
        painter = painterResource(R.drawable.dishipoa_logo_black),
        contentDescription = "DishiPoa logo",
        modifier = Modifier.size(size)
    )
}
// The logo, name and tagline at the top of the login screen
@Composable
fun AuthLogo(tagline: String, showName: Boolean = true) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoImage(size = 96.dp)
        Text("", fontSize = 46.sp)
        Spacer(Modifier.height(4.dp))

        // "Dishi" in green and "Poa" in saffron, in a serif font like the mockups
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Dishi") }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append("Poa") }
            },
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tagline,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// White boxes for the text fields, so they stand out on the cream background
@Composable
fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextDark,        // the text the user types
    unfocusedTextColor = TextDark,
    cursorColor = FoodGreen,
    focusedContainerColor = White,      // white box behind the text
    unfocusedContainerColor = White,
    focusedBorderColor = FoodGreen,
    unfocusedBorderColor = Line,
    focusedLabelColor = FoodGreen,
    unfocusedLabelColor = Muted
)
