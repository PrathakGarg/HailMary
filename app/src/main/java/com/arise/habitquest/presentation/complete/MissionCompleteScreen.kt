package com.arise.habitquest.presentation.complete

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.ui.components.*
import com.arise.habitquest.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MissionCompleteUiState(
    val missionTitle: String = "",
    val xpGained: Int = 0,
    val statGains: Map<String, Int> = emptyMap(),
    val didLevelUp: Boolean = false,
    val newLevel: Int = 1,
    val newAchievements: Int = 0,
    val promotedToShadow: Boolean = false
)

@HiltViewModel
class MissionCompleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository
) : ViewModel() {
    private val missionId: String = checkNotNull(savedStateHandle["missionId"])

    private val _state = MutableStateFlow(MissionCompleteUiState())
    val state: StateFlow<MissionCompleteUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val mission = missionRepository.getMissionById(missionId)
            mission?.let { m ->
                _state.update {
                    it.copy(missionTitle = m.title, xpGained = m.effectiveXpReward)
                }
            }
        }
    }
}

@Composable
fun MissionCompleteScreen(
    missionId: String,
    onReturn: () -> Unit,
    viewModel: MissionCompleteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle(initialValue = MissionCompleteUiState())

    var showParticles by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100); showParticles = true
        delay(500); showContent = true
        delay(2200); showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mission_complete_screen")
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldCore.copy(alpha = 0.08f), BackgroundDeep),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        ParticleBurst(
            modifier = Modifier.fillMaxSize(),
            trigger = showParticles,
            primaryColor = GoldCore,
            secondaryColor = EmeraldCore,
            particleCount = 150
        )

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(600)) + scaleIn(tween(500))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "✓",
                    style = AriseTypography.displayLarge.copy(color = EmeraldCore, fontSize = 72.sp)
                )
                Text(
                    "GATE CLEARED",
                    style = AriseTypography.headlineLarge.copy(color = TextPrimary, letterSpacing = 4.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    state.missionTitle,
                    style = AriseTypography.titleMedium.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // XP gained
                AnimatedCounter(
                    targetValue = state.xpGained,
                    prefix = "+",
                    suffix = " XP",
                    color = GoldCore
                )

                // Stat gains
                for ((stat, amount) in state.statGains) {
                    Text(
                        "+$amount $stat",
                        style = AriseTypography.titleSmall.copy(color = PurpleLight)
                    )
                }

                if (state.didLevelUp) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(PurpleCore, GoldCore)),
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "LEVEL UP → ${state.newLevel}",
                            style = AriseTypography.headlineSmall.copy(color = TextPrimary)
                        )
                    }
                }

                if (state.promotedToShadow) {
                    Text(
                        "★ SHADOW PROMOTED",
                        style = AriseTypography.titleMedium.copy(color = PurpleLight),
                        textAlign = TextAlign.Center
                    )
                }

                AnimatedVisibility(
                    visible = showButton,
                    enter = fadeIn(tween(400)) + slideInVertically()
                ) {
                    Button(
                        onClick = onReturn,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleCore),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mission_complete_return")
                            .glowEffect(PurpleGlow)
                    ) {
                        Text("RETURN TO BASE", style = AriseTypography.labelLarge.copy(color = TextPrimary))
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCounter(
    targetValue: Int,
    prefix: String = "",
    suffix: String = "",
    color: Color = GoldCore
) {
    var displayValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(targetValue) {
        val step = (targetValue / 20).coerceAtLeast(1)
        while (displayValue < targetValue) {
            displayValue = (displayValue + step).coerceAtMost(targetValue)
            delay(30)
        }
    }
    Text(
        "$prefix$displayValue$suffix",
        style = AriseTypography.displaySmall.copy(color = color)
    )
}

