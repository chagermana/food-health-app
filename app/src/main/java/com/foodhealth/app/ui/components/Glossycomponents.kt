package com.foodhealth.app.ui.components

// ===========IMPORTS =====================
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.Meal
import com.foodhealth.app.ui.theme.CarbColor
import com.foodhealth.app.ui.theme.FatColor
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.FoodOrange
import com.foodhealth.app.ui.theme.Line
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.ProteinColor
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.White

// ==========================================
// Glossy IOS look
// Reused by every screen
// Includes glossy buttons, three-dots menu, top bar, food cards, ring and bars
// ==========================================

// ----------- Glossy round button (white, soft shadow, light top highlight) -------
@Composable
fun GlossyIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(8.dp, CircleShape, ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF3EFE6))))
            .border(1.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = TextDark, modifier = Modifier.size(size * 0.45f))
    }
}

// Universal, back arrow.
@Composable
fun GlossyBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    GlossyIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack, modifier)
}

// One extra entry in the three-dots menu.
data class MenuAction(val label: String, val icon: ImageVector, val onClick: () -> Unit)

// The glossy three dots. Opens a dropdown menu: first item is always
// "About this screen" (shows a small dialog), then any extra actions.
@Composable
fun InfoMenuButton(
    infoTitle: String,
    infoText: String,
    extraActions: List<MenuAction> = emptyList()
) {
    var expanded by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    Box {
        GlossyIconButton(Icons.Filled.MoreHoriz, "More options", { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = White
        ) {
            DropdownMenuItem(
                text = { Text("About this screen") },
                leadingIcon = { Icon(Icons.Outlined.Info, null, tint = FoodGreen) },
                onClick = { expanded = false; showInfo = true }
            )
            extraActions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label) },
                    leadingIcon = { Icon(action.icon, null, tint = FoodGreen) },
                    onClick = { expanded = false; action.onClick() }
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = White,
            title = { Text(infoTitle, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text(infoText, fontSize = 14.sp, color = Muted) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Got it", color = FoodGreen, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

// Top bar used by Portion, Analysis and Rate:  (<)   Title   (...)
@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    infoText: String,
    modifier: Modifier = Modifier,
    extraActions: List<MenuAction> = emptyList()
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlossyBackButton(onBack)
        Text(
            title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextDark
        )
        InfoMenuButton(title, infoText, extraActions)
    }
}

// ---------- Small frosted-glass pill with a coloured dot: "28g Protein" ----------
@Composable
fun NutriPill(value: String, tag: String, dot: Color) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.85f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(4.dp))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.width(3.dp))
            Text(tag, fontSize = 9.sp, color = Muted)
        }
    }
}

// ---------- Home grid card: photo, nutrition pills on the LEFT, meal name at the BOTTOM ----------
@Composable
fun MealNutriCard(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 210.dp
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .height(height)
            .shadow(10.dp, shape, ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(meal.imageRes),
            contentDescription = meal.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // dark fade so text is always readable
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.55f to Color(0x11000000),
                    1f to Color(0xCC000000)
                )
            )
        )
        // nutrition pills, left side
        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NutriPill("${meal.calories}", "kcal", FoodOrange)
        }
        // meal name, bottom
        Row(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                meal.name,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = FoodGreen, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ---------- Calorie ring (works on light AND dark backgrounds via colour params) ----------
@Composable
fun CalorieRing(
    progress: Float,
    centerText: String,
    subText: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 150.dp,
    trackColor: Color = Line,
    progressColor: Color = FoodGreen,
    textColor: Color = TextDark,
    subColor: Color = Muted
) {
    Box(modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 14.dp.toPx()
            drawArc(
                color = trackColor, startAngle = -90f, sweepAngle = 360f,
                useCenter = false, style = Stroke(strokeWidth)
            )
            drawArc(
                color = progressColor, startAngle = -90f, sweepAngle = 360f * progress,
                useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerText, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = textColor)
            Text(subText, fontSize = 11.sp, color = subColor)
        }
    }
}

// ---------- "Protein ........ 96 / 140 g" with a coloured level bar under it ----------
@Composable
fun LabeledBar(
    label: String,
    valueText: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    labelColor: Color = TextDark,
    trackColor: Color = Line
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = labelColor)
            Text(valueText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = labelColor)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 10.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(9.dp)),
            color = color,
            trackColor = trackColor
        )
    }
}

