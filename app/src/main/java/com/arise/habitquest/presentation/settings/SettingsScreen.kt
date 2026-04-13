package com.arise.habitquest.presentation.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.FocusTheme
import com.arise.habitquest.ui.components.glowEffect
import com.arise.habitquest.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile
    var showRegenerateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSurface)
                    .padding(top = 44.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = TextSecondary)
                    }
                    Text("SETTINGS", style = AriseTypography.headlineSmall.copy(letterSpacing = 3.sp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_screen")
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Life themes ───────────────────────────────────────────────
            SettingsSection("LIFE FOCUS AREAS") {
                Text(
                    "Select up to 3 themes. The System will bias your daily gates toward these areas.",
                    style = AriseTypography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(4.dp))
                FocusThemeGrid(
                    activeThemes = state.activeFocusThemes,
                    onToggle = viewModel::toggleFocusTheme
                )
                Text(
                    "${state.activeFocusThemes.size}/3 active  ·  At least 1 required",
                    style = AriseTypography.labelSmall.copy(color = TextDim),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_focus_count")
                )
            }

            // ── Rest day ──────────────────────────────────────────────────
            SettingsSection("REST DAY") {
                DayOfWeek.entries.forEach { day ->
                    val selected = profile?.restDay == day.ordinal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_rest_day_${day.name.lowercase()}")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) PurpleFaint else BackgroundCard)
                            .border(1.dp, if (selected) PurpleCore else BorderDefault, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setRestDay(day.ordinal) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day.name, style = AriseTypography.bodyMedium.copy(color = if (selected) TextPrimary else TextSecondary))
                        if (selected) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                null,
                                tint = PurpleCore,
                                modifier = Modifier
                                    .size(18.dp)
                                    .testTag("settings_rest_day_selected_${day.name.lowercase()}")
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "After changing your rest day, regenerate the current mission day to apply the new schedule immediately.",
                    style = AriseTypography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showRegenerateDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_regenerate_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleCore),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PurpleCore)
                    )
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("REGENERATE CURRENT MISSIONS", style = AriseTypography.labelMedium)
                }
            }

            // ── Day start time ────────────────────────────────────────────
            SettingsSection("DAY START TIME") {
                val slot = state.dayStartMinutes / 30f
                val formattedTime = formatDayStartTime(state.dayStartMinutes)
                Text(
                    "New missions unlock at $formattedTime. Previous day's missions expire at this time.",
                    style = AriseTypography.bodySmall.copy(color = TextSecondary)
                )
                Slider(
                    value = slot,
                    onValueChange = { viewModel.setDayStartMinutes((it.toInt() * 30)) },
                    modifier = Modifier.testTag("settings_day_start_slider"),
                    valueRange = 0f..47f,
                    steps = 46,
                    colors = SliderDefaults.colors(
                        thumbColor = PurpleCore,
                        activeTrackColor = PurpleCore,
                        inactiveTrackColor = BorderDefault
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("12:00 AM", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp))
                    Text("11:30 PM", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp))
                }
            }

            // ── Notification time ─────────────────────────────────────────
            SettingsSection("MORNING NOTIFICATION") {
                Text(
                    "Reminder at: ${state.notificationHour}:00",
                    style = AriseTypography.bodyMedium.copy(color = TextPrimary),
                    modifier = Modifier.testTag("settings_notification_label")
                )
                Slider(
                    value = state.notificationHour.toFloat(),
                    onValueChange = { viewModel.setNotificationHour(it.toInt()) },
                    modifier = Modifier.testTag("settings_notification_slider"),
                    valueRange = 5f..22f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = PurpleCore,
                        activeTrackColor = PurpleCore,
                        inactiveTrackColor = BorderDefault
                    )
                )
            }

            // ── Emergency stasis ─────────────────────────────────────────
            SettingsSection("EMERGENCY STASIS") {
                Text(
                    "Activates a 24-hour grace period. No penalties will apply. Uses remaining: ${profile?.graceUsesRemaining ?: 0}/3",
                    style = AriseTypography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.testTag("settings_stasis_uses")
                )
                Spacer(Modifier.height(4.dp))
                val canUse = (profile?.graceUsesRemaining ?: 0) > 0
                Button(
                    onClick = viewModel::activateEmergencyGrace,
                    enabled = canUse,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldCore,
                        contentColor = androidx.compose.ui.graphics.Color(0xFF0A0A0F),
                        disabledContainerColor = BackgroundElevated,
                        disabledContentColor = TextDim
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_stasis_button")
                ) {
                    Icon(Icons.Filled.Shield, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ACTIVATE EMERGENCY STASIS", style = AriseTypography.labelMedium)
                }
                Text(
                    "Use only for real life events. The System does not forget abuse.",
                    style = SystemTextStyle.copy(color = TextDim, fontSize = 11.sp)
                )
            }

            // ── About ─────────────────────────────────────────────────────
            SettingsSection("SYSTEM") {
                Text("ARISE v1.0", style = AriseTypography.bodyMedium.copy(color = TextSecondary))
                Text(
                    "\"The System does not sleep. It does not forgive. But it rewards relentlessly.\"",
                    style = SystemTextStyle.copy(color = TextDim, fontSize = 11.sp)
                )
            }
        }

        if (showRegenerateDialog) {
            AlertDialog(
                onDismissRequest = { showRegenerateDialog = false },
                modifier = Modifier.testTag("settings_regenerate_dialog"),
                containerColor = BackgroundSurface,
                title = {
                    Text(
                        "Regenerate Current Missions?",
                        style = AriseTypography.titleMedium.copy(color = TextPrimary)
                    )
                },
                text = {
                    Text(
                        "This will replace the current session day's daily missions using your updated rest day and current focus settings.",
                        style = AriseTypography.bodyMedium.copy(color = TextSecondary)
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRegenerateDialog = false },
                        modifier = Modifier.testTag("settings_regenerate_cancel")
                    ) {
                        Text("CANCEL", style = AriseTypography.labelMedium.copy(color = TextDim))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRegenerateDialog = false
                            viewModel.regenerateMissions()
                        },
                        modifier = Modifier.testTag("settings_regenerate_confirm")
                    ) {
                        Text("REGENERATE", style = AriseTypography.labelMedium.copy(color = PurpleCore))
                    }
                }
            )
        }
    }
}

private fun formatDayStartTime(minutes: Int): String {
    val normalized = (minutes / 30).coerceIn(0, 47) * 30
    val t = LocalTime.of(normalized / 60, normalized % 60)
    return t.format(DateTimeFormatter.ofPattern("h:mm a"))
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = AriseTypography.labelMedium.copy(color = PurpleLight, letterSpacing = 2.sp)
        )
        content()
    }
}

@Composable
private fun FocusThemeGrid(
    activeThemes: Set<FocusTheme>,
    onToggle: (FocusTheme) -> Unit
) {
    val themes = FocusTheme.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.chunked(2).forEach { rowThemes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowThemes.forEach { theme ->
                    val isActive = theme in activeThemes
                    val canActivate = isActive || activeThemes.size < 3
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_theme_${theme.name.lowercase()}")
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) PurpleFaint else BackgroundCard)
                            .border(
                                1.dp,
                                if (isActive) PurpleCore else BorderDefault,
                                RoundedCornerShape(10.dp)
                            )
                            .then(if (canActivate || isActive) Modifier.clickable { onToggle(theme) } else Modifier)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    theme.displayName,
                                    style = AriseTypography.labelMedium.copy(
                                        color = if (isActive) TextPrimary else TextSecondary
                                    )
                                )
                                if (isActive) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        null,
                                        tint = PurpleCore,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                theme.subtitle,
                                style = AriseTypography.bodySmall.copy(
                                    color = if (isActive) TextSecondary else TextDim,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
                // Fill empty slot if odd number in row
                if (rowThemes.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
