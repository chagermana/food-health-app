package com.foodhealth.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.data.ScanSource
import com.foodhealth.app.ui.MealFlowState
import com.foodhealth.app.ui.theme.DeepGreen
import com.foodhealth.app.ui.theme.FoodGreen
import com.foodhealth.app.ui.theme.Lime
import com.foodhealth.app.ui.theme.Muted
import com.foodhealth.app.ui.theme.SoftGreen
import com.foodhealth.app.ui.theme.TextDark
import com.foodhealth.app.ui.theme.White

// The meal's picture with the confirmed foods written on a dark fade.
// Shows the user's photo, or the preset meal's drawable for manual picks.
@Composable
fun FlowPhotoCard(flow: MealFlowState, modifier: Modifier = Modifier, height: Dp = 180.dp) {
    val title = flow.selected.joinToString(" + ") { it.displayName }
    val bitmap = flow.photo
    val preset = flow.presetImageRes

    when {
        bitmap != null -> Box(
            modifier = modifier
                .height(height)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000))))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    title, color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (flow.source != ScanSource.MANUAL) {
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Lime)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = DeepGreen, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI scan", color = DeepGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        preset != null -> MealPhotoCard(
            title = title, imageRes = preset, modifier = modifier,
            height = height, labelSize = 14.sp
        )
    }
}

// "Take a photo" or "Upload from gallery" chooser, opened by the Home scan card.
@Composable
fun ScanSourceDialog(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = White,
        title = {
            Text("Scan your meal", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SourceOption(Icons.Outlined.CameraAlt, "Take a photo", "Use your camera", onCamera)
                SourceOption(Icons.Outlined.PhotoLibrary, "Upload from gallery", "Pick a photo from your phone", onGallery)
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) } }
    )
}

@Composable
private fun SourceOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = SoftGreen,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = FoodGreen, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 15.sp)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
        }
    }
}