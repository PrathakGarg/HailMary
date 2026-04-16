package com.arise.habitquest.presentation.progression

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.ui.theme.AriseTypography
import com.arise.habitquest.ui.theme.BackgroundCard
import com.arise.habitquest.ui.theme.BlueCore
import com.arise.habitquest.ui.theme.CrimsonCore
import com.arise.habitquest.ui.theme.TextPrimary
import com.arise.habitquest.ui.theme.TextSecondary

@Composable
fun ProgramDirectivesSection(
    directives: List<ProgramDirective>,
    modifier: Modifier = Modifier,
    tagPrefix: String = "program_directive"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        directives.forEachIndexed { index, directive ->
            ProgramDirectiveCard(
                directive = directive,
                modifier = Modifier.testTag("${tagPrefix}_$index")
            )
        }
    }
}

@Composable
fun ProgramDirectiveCard(
    directive: ProgramDirective,
    modifier: Modifier = Modifier
) {
    val accent = if (directive.isWarning) CrimsonCore else BlueCore
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundCard, RoundedCornerShape(10.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                directive.label,
                style = AriseTypography.labelSmall.copy(
                    color = accent,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            )
            Text(
                directive.value,
                style = AriseTypography.titleSmall.copy(color = TextPrimary)
            )
            Text(
                directive.detail,
                style = AriseTypography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }
    }
}