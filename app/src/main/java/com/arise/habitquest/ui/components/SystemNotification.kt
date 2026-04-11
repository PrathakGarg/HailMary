package com.arise.habitquest.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.ui.theme.*
import kotlinx.coroutines.delay

/**
 * In-app "System" notification — slides up from bottom, auto-dismisses.
 */
@Composable
fun SystemNotification(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    type: NotificationType = NotificationType.INFO,
    autoDismissMs: Long = 3000L
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(autoDismissMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BackgroundElevated)
                .border(
                    1.dp,
                    type.borderColor,
                    RoundedCornerShape(12.dp)
                )
                .glowEffect(type.glowColor, radius = 8.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // System label
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "[ SYSTEM ]",
                        style = AriseTypography.labelSmall.copy(
                            color = type.borderColor,
                            letterSpacing = 3.sp,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message,
                        style = SystemTextStyle.copy(color = TextSystem, fontSize = 13.sp)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = TextDim,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

enum class NotificationType(val borderColor: Color, val glowColor: Color) {
    INFO(PurpleCore, PurpleGlow),
    SUCCESS(EmeraldCore, EmeraldCore.copy(alpha = 0.4f)),
    WARNING(GoldCore, GoldGlow),
    DANGER(CrimsonCore, CrimsonGlow)
}
