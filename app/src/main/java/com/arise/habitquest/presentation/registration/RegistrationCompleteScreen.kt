package com.arise.habitquest.presentation.registration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.Stat
import com.arise.habitquest.presentation.profile.ProfileViewModel
import com.arise.habitquest.ui.components.*
import com.arise.habitquest.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RegistrationCompleteScreen(
    onEnterApp: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.profile.collectAsStateWithLifecycle()
    val profile = profileState ?: return

    var showParticles by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showName by remember { mutableStateOf(false) }
    var showRank by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300); showParticles = true
        delay(400); showTitle = true
        delay(600); showName = true
        delay(800); showRank = true
        delay(1200); showStats = true
        delay(2500); showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(PurpleCore.copy(alpha = 0.12f), BackgroundDeep),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Particle burst behind everything
        ParticleBurst(
            modifier = Modifier.fillMaxSize(),
            trigger = showParticles,
            primaryColor = GoldCore,
            secondaryColor = PurpleCore
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(600)) + slideInVertically()
            ) {
                Text(
                    "HUNTER REGISTRATION",
                    style = AriseTypography.labelLarge.copy(
                        color = PurpleLight, letterSpacing = 4.sp, fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            AnimatedVisibility(visible = showTitle, enter = fadeIn(tween(400))) {
                Text(
                    "COMPLETE",
                    style = AriseTypography.displayMedium.copy(color = TextPrimary),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = showName, enter = fadeIn(tween(600))) {
                Text(
                    profile.hunterName.uppercase(),
                    style = AriseTypography.headlineLarge.copy(color = GoldCore),
                    textAlign = TextAlign.Center
                )
            }
            AnimatedVisibility(visible = showName, enter = fadeIn(tween(600))) {
                Text(
                    "\"${profile.epithet}\"",
                    style = AriseTypography.titleMedium.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(visible = showRank, enter = scaleIn() + fadeIn(tween(600))) {
                RankBadge(rank = profile.rank, size = 72.dp)
            }

            AnimatedVisibility(visible = showStats, enter = fadeIn(tween(800))) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "INITIAL STATS",
                        style = AriseTypography.labelMedium.copy(color = TextDim, letterSpacing = 2.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Stat.entries.forEach { stat ->
                        StatBar(stat = stat, value = profile.stats.get(stat), maxValue = 100)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = showButton, enter = fadeIn(tween(600)) + slideInVertically()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ARISE, ${profile.hunterName.uppercase()}.",
                        style = SystemTextStyle.copy(color = PurpleLight, fontSize = 14.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Your journey begins.",
                        style = SystemTextStyle.copy(color = TextSystem),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onEnterApp,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleCore),
                        modifier = Modifier
                            .fillMaxWidth()
                            .glowEffect(PurpleGlow)
                    ) {
                        Text(
                            "▲  ENTER THE SYSTEM",
                            style = AriseTypography.labelLarge.copy(color = TextPrimary)
                        )
                    }
                }
            }
        }
    }
}
