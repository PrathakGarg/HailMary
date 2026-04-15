package com.arise.habitquest.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.ui.theme.AriseTypography
import com.arise.habitquest.ui.theme.TextDim

@Composable
fun SectionLabel(label: String) {
    Text(
        label,
        style = AriseTypography.labelSmall.copy(color = TextDim, letterSpacing = 2.sp),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
