package com.foodhealth.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.White

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

// A rounded pill the user can tap for portion size, cooking method, etc
@Composable
fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) FoodGreen else White,
        contentColor = if (selected) White else TextDark,
        border = BorderStroke(1.5.dp, if (selected) FoodGreen else Line)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

// A photo with the meal name on a dark fade at the bottom (home grid0
// onclick = null, meaning the photo is not tappable/clickable
@Composable
fun MealPhotoCard(
    title: String,
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    height: Dp = 104.dp,
    labelSize: TextUnit = 12.sp,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .then(clickModifier)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        //transparent -> dark fade for white to be always readable
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000))))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = labelSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// A white rounded card with a thin border.
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SmallLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 12.sp, color = Muted, modifier = modifier)
}

@Composable
fun BackHeader(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onBack)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = FoodGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text("Back", color = FoodGreen, fontSize = 14.sp)
    }
}

// Shown when there is nothing to display on the screen
// e.g Analysis tab when nothing is being displayed
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = FoodGreen, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(buttonText, onButtonClick)
    }
}