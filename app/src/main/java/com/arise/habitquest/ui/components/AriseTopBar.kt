package com.arise.habitquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.ui.theme.AriseTypography
import com.arise.habitquest.ui.theme.BackgroundSurface
import com.arise.habitquest.ui.theme.TextSecondary

@Composable
fun AriseTopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    background: Color = BackgroundSurface,
    gradientBackground: Brush? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val bgModifier = if (gradientBackground != null) {
        Modifier.fillMaxWidth().background(gradientBackground)
    } else {
        Modifier.fillMaxWidth().background(background)
    }

    val startPad = if (onBack != null) 8.dp else 16.dp

    Box(
        modifier = bgModifier.padding(top = 44.dp, start = startPad, end = 16.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (trailingContent != null) Arrangement.SpaceBetween else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = TextSecondary)
                    }
                }
                if (title != null) {
                    Text(title, style = AriseTypography.headlineSmall.copy(letterSpacing = 3.sp))
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
