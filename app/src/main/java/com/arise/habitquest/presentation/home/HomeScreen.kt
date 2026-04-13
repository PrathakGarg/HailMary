package com.arise.habitquest.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.ui.components.*
import com.arise.habitquest.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onNavigateToMissions: () -> Unit,
    onNavigateToMissionDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRankUp: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    bottomBarPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to rank-up screen when a rank-up is pending
    LaunchedEffect(state.pendingRankUp) {
        if (state.pendingRankUp.isNotEmpty()) {
            viewModel.clearPendingRankUp()
            onNavigateToRankUp(state.pendingRankUp)
        }
    }

    Scaffold(containerColor = BackgroundDeep) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(bottom = bottomBarPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Header ──────────────────────────────────────────────────
                state.profile?.let { profile ->
                    HomeHeader(
                        profile = profile,
                        onProfileClick = onNavigateToProfile,
                        onSettingsClick = onNavigateToSettings
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── System message ──────────────────────────────────────────
                state.profile?.let {
                    SystemMessageCard(
                        message = if (state.todayMissions.isEmpty())
                            "Awaiting today's gates to be assigned, ${it.hunterName}. Stand ready."
                        else
                            "ACTIVE GATES: ${state.completedCount}/${state.totalCount} CLEARED. The System watches your progress."
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Time-almost-up warning ──────────────────────────────────
                val hasIncomplete = state.todayMissions.any { it.isActive }
                if (state.minutesUntilReset < 120 && hasIncomplete) {
                    TimeRemainingWarning(
                        minutesUntilReset = state.minutesUntilReset,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── Today's gates section ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TODAY'S GATES",
                        style = AriseTypography.labelLarge.copy(color = PurpleLight, letterSpacing = 3.sp)
                    )
                    TextButton(onClick = onNavigateToMissions) {
                        Text("VIEW ALL", style = AriseTypography.labelSmall.copy(color = TextDim))
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (state.todayMissions.isEmpty()) {
                    EmptyGatesCard()
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.todayMissions) { mission ->
                            MissionCard(
                                mission = mission,
                                onClick = { onNavigateToMissionDetail(mission.id) },
                                onComplete = if (mission.isActive) {
                                    { viewModel.quickCompleteMission(mission) }
                                } else null,
                                modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Quick stats row ─────────────────────────────────────────
                state.profile?.let { profile ->
                    QuickStatsRow(
                        streakDays = profile.streakCurrent,
                        completedToday = state.completedCount,
                        totalToday = state.totalCount,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Date ────────────────────────────────────────────────────
                Text(
                    state.currentDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")).uppercase(),
                    style = AriseTypography.labelSmall.copy(color = TextDim),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            // System notification overlay (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                SystemNotification(
                    message = state.notificationMessage,
                    visible = state.showSystemNotification,
                    onDismiss = viewModel::dismissNotification,
                    type = NotificationType.SUCCESS
                )
            }
        }
    }
}

@Composable
fun HomeHeader(
    profile: com.arise.habitquest.domain.model.UserProfile,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundSurface, BackgroundDeep)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Top row: rank badge + name + settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable(onClick = onProfileClick)
                ) {
                    RankBadge(rank = profile.rank, size = 44.dp, pulsing = false)
                    Column {
                        Text(
                            profile.hunterName.uppercase(),
                            style = AriseTypography.titleMedium.copy(color = TextPrimary)
                        )
                        Text(
                            "\"${profile.title}\"",
                            style = AriseTypography.bodySmall.copy(color = TextDim)
                        )
                    }
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(Icons.Filled.Settings, "Settings", tint = TextDim)
                }
            }

            // HP & XP bars
            HpBar(
                current = profile.hp,
                max = profile.maxHp,
                damaged = profile.pendingWarning
            )
            XpBar(current = profile.xp, max = profile.xpToNextLevel)

            // Rank + level label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "RANK ${profile.rank.displayName}  ·  LEVEL ${profile.level}",
                    style = AriseTypography.labelSmall.copy(color = rankColor(profile.rank), letterSpacing = 1.5.sp)
                )
                Text(
                    "TOTAL XP: ${profile.totalXpEarned}",
                    style = AriseTypography.labelSmall.copy(color = TextDim)
                )
            }
        }
    }
}

@Composable
fun SystemMessageCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .border(1.dp, BorderAccent, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "[ SYSTEM ]",
                style = AriseTypography.labelSmall.copy(color = PurpleLight, letterSpacing = 3.sp, fontSize = 9.sp)
            )
            Text(message, style = SystemTextStyle)
        }
    }
}

@Composable
fun EmptyGatesCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundCard)
            .border(1.dp, BorderDefault, RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("▲", style = AriseTypography.displaySmall.copy(color = PurpleDim))
            Text("REST DAY", style = AriseTypography.labelLarge.copy(color = TextSecondary))
            Text(
                "The System grants you respite. Recover your strength.",
                style = SystemTextStyle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickStatsRow(
    streakDays: Int,
    completedToday: Int,
    totalToday: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip("🔥 STREAK", "$streakDays days", GoldCore, Modifier.weight(1f))
        StatChip("✓ TODAY", "$completedToday / $totalToday", EmeraldCore, Modifier.weight(1f))
    }
}

@Composable
fun StatChip(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundCard)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp, letterSpacing = 1.sp))
            Spacer(Modifier.height(2.dp))
            Text(value, style = AriseTypography.titleMedium.copy(color = accentColor))
        }
    }
}

@Composable
fun TimeRemainingWarning(minutesUntilReset: Long, modifier: Modifier = Modifier) {
    val isUrgent = minutesUntilReset < 30
    val accentColor = if (isUrgent) CrimsonCore else GoldCore
    val hours = minutesUntilReset / 60
    val mins = minutesUntilReset % 60
    val timeStr = when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
    val message = if (isUrgent)
        "CRITICAL — $timeStr REMAINING. CLOSE YOUR GATES NOW."
    else
        "$timeStr REMAINING — INCOMPLETE GATES DETECTED"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                message,
                style = AriseTypography.labelSmall.copy(color = accentColor, letterSpacing = 1.sp)
            )
        }
    }
}

