package com.arise.habitquest.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.domain.model.*
import com.arise.habitquest.ui.theme.*

// Crimson used for System Mandate badge — distinct from category colours
private val MandateBadgeColor = CrimsonCore
private val MandateBorderColor = PurpleCore

@Composable
fun MissionCard(
    mission: Mission,
    onClick: () -> Unit,
    onComplete: (() -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val diffColor = difficultyColor(mission.difficulty)
    val categoryColor = categoryColor(mission.category)
    val isCompleted = mission.isCompleted
    val isFailed = mission.isFailed
    val isMandate = mission.isSystemMandate

    val cardAlpha = when {
        isCompleted || isFailed -> 0.5f
        else -> 1f
    }

    // Mandate cards pulse their purple border; regular cards glow their category colour
    val borderColor = if (isMandate && !isCompleted && !isFailed)
        MandateBorderColor
    else
        categoryColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}")
            .clickable(onClick = onClick)
            .then(
                if (!isCompleted && !isFailed)
                    Modifier.glowEffect(borderColor.copy(alpha = if (isMandate) 0.5f else 0.3f), radius = if (isMandate) 8.dp else 6.dp)
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMandate && !isCompleted && !isFailed)
                Color(0xFF1A0A2E).copy(alpha = cardAlpha)   // deep purple tint for mandate
            else
                BackgroundCard.copy(alpha = cardAlpha)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when {
                        isCompleted -> Modifier.testTag("mission_status_completed_${mission.id}")
                        isFailed -> Modifier.testTag("mission_status_failed_${mission.id}")
                        else -> Modifier
                    }
                )
                .border(
                    width = if (isMandate && !isCompleted && !isFailed) 1.5.dp else 1.dp,
                    brush = Brush.horizontalGradient(
                        if (isMandate && !isCompleted && !isFailed)
                            listOf(MandateBorderColor.copy(alpha = 0.8f), MandateBorderColor.copy(alpha = 0.2f))
                        else
                            listOf(
                                categoryColor.copy(alpha = if (isCompleted) 0.2f else 0.6f),
                                BorderDefault.copy(alpha = 0.3f)
                            )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // System Mandate header strip
                if (isMandate && !isCompleted && !isFailed) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(MandateBorderColor.copy(alpha = 0.25f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MandateBadgeColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "SYSTEM MANDATE",
                            style = AriseTypography.labelSmall.copy(
                                color = MandateBadgeColor,
                                fontSize = 9.sp,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "ALL-ROUND GROWTH",
                            style = AriseTypography.labelSmall.copy(
                                color = MandateBorderColor.copy(alpha = 0.7f),
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category icon bubble — mandate uses purple tint
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isMandate && !isCompleted && !isFailed)
                                    MandateBorderColor.copy(alpha = 0.15f)
                                else categoryColor.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                if (isMandate && !isCompleted && !isFailed)
                                    MandateBorderColor.copy(alpha = 0.4f)
                                else categoryColor.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = categoryIcon(mission.category),
                            contentDescription = null,
                            tint = when {
                                isCompleted -> categoryColor.copy(alpha = 0.4f)
                                isMandate -> MandateBorderColor.copy(alpha = 0.9f)
                                else -> categoryColor
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Title + meta
                    Column(modifier = Modifier.weight(1f)) {
                        // Tags row: difficulty + streak
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(diffColor.copy(alpha = 0.15f))
                                    .border(0.5.dp, diffColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "RANK ${mission.difficulty.displayName}",
                                    style = AriseTypography.labelSmall.copy(
                                        color = diffColor,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                            if (mission.streakCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = GoldCore,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        "${mission.streakCount}",
                                        style = AriseTypography.labelSmall.copy(
                                            color = GoldCore, fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = mission.title,
                            style = AriseTypography.titleSmall.copy(
                                color = if (isCompleted) TextDim else TextPrimary,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        // XP + time hint row
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = GoldCore, modifier = Modifier.size(12.dp))
                            Text(
                                text = "+${mission.xpReward} XP",
                                style = AriseTypography.labelSmall.copy(color = GoldCore, fontSize = 10.sp)
                            )
                            if (isMandate) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "• no HP penalty",
                                    style = AriseTypography.labelSmall.copy(
                                        color = MandateBorderColor.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                )
                            } else if (mission.scheduledTimeHint != null) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = mission.scheduledTimeHint,
                                    style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp)
                                )
                            }
                        }
                    }

                    // Right side: status or complete button
                    when {
                        isCompleted -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = EmeraldCore,
                                    modifier = Modifier.size(24.dp)
                                )
                                if (onReset != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable(onClick = onReset)
                                            .background(PurpleFaint)
                                            .testTag("mission_reset_${mission.id}")
                                    ) {
                                        Icon(
                                            Icons.Filled.Restore,
                                            contentDescription = "Reset mission",
                                            tint = TextSecondary,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                        isFailed -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Cancel,
                                    contentDescription = "Failed",
                                    tint = CrimsonCore,
                                    modifier = Modifier.size(24.dp)
                                )
                                if (onReset != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable(onClick = onReset)
                                            .background(PurpleFaint)
                                            .testTag("mission_reset_${mission.id}")
                                    ) {
                                        Icon(
                                            Icons.Filled.Restore,
                                            contentDescription = "Reset mission",
                                            tint = TextSecondary,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                        onComplete != null -> {
                            IconButton(
                                onClick = onComplete,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("mission_quick_complete_${mission.id}")
                                    .clip(CircleShape)
                                    .background(if (isMandate) MandateBorderColor.copy(alpha = 0.15f) else PurpleFaint)
                                    .border(1.dp, if (isMandate) MandateBorderColor.copy(alpha = 0.5f) else PurpleCore.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Filled.CheckBox,
                                    contentDescription = "Complete",
                                    tint = if (isMandate) MandateBorderColor else PurpleLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.F -> RankColorE
    Difficulty.E -> EmeraldCore
    Difficulty.D -> BlueCore
    Difficulty.C -> StatSenseColor
    Difficulty.B -> GoldCore
    Difficulty.A -> CrimsonCore
    Difficulty.S -> RankColorSS
}

fun categoryColor(category: MissionCategory): Color = when (category) {
    MissionCategory.PHYSICAL -> StatStrColor
    MissionCategory.MENTAL -> StatIntColor
    MissionCategory.PRODUCTIVITY -> StatAgiColor
    MissionCategory.SOCIAL -> StatSenseColor
    MissionCategory.WELLNESS -> StatVitColor
    MissionCategory.CREATIVITY -> StatEndColor
}

fun categoryIcon(category: MissionCategory) = when (category) {
    MissionCategory.PHYSICAL -> Icons.Filled.FitnessCenter
    MissionCategory.MENTAL -> Icons.Filled.Psychology
    MissionCategory.PRODUCTIVITY -> Icons.Filled.TaskAlt
    MissionCategory.SOCIAL -> Icons.Filled.Group
    MissionCategory.WELLNESS -> Icons.Filled.SelfImprovement
    MissionCategory.CREATIVITY -> Icons.Filled.Palette
}
