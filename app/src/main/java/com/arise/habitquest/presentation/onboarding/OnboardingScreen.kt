package com.arise.habitquest.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.*
import com.arise.habitquest.ui.components.glowEffect
import com.arise.habitquest.ui.theme.*
import java.time.DayOfWeek

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Progress indicator
            OnboardingProgressBar(currentPhase = state.phase, totalPhases = 6)

            // Phase content
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                label = "phase_content"
            ) { phase ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (phase) {
                        0 -> Phase0Awakening(state, viewModel)
                        1 -> Phase1Goals(state, viewModel)
                        2 -> Phase2Status(state, viewModel)
                        3 -> Phase3Personality(state, viewModel)
                        4 -> Phase4History(state, viewModel)
                        5 -> Phase5Schedule(state, viewModel)
                    }
                }
            }

            // Navigation
            OnboardingNavBar(
                phase = state.phase,
                canProceed = state.canProceed,
                isLoading = state.isLoading,
                onBack = viewModel::previousPhase,
                onNext = {
                    if (state.phase < 5) viewModel.nextPhase()
                    else viewModel.completeOnboarding()
                }
            )
        }
    }
}

@Composable
fun OnboardingProgressBar(currentPhase: Int, totalPhases: Int) {
    val phaseNames = listOf("Awakening", "Goals", "Status", "Mindset", "History", "Schedule")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundSurface)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PHASE ${currentPhase + 1} / $totalPhases",
                style = AriseTypography.labelSmall.copy(color = PurpleLight, letterSpacing = 2.sp)
            )
            Text(
                phaseNames.getOrElse(currentPhase) { "" }.uppercase(),
                style = AriseTypography.labelMedium.copy(color = TextSecondary)
            )
        }
        Spacer(Modifier.height(8.dp))
        // Segmented progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(totalPhases) { i ->
                val fraction by animateFloatAsState(
                    targetValue = if (i <= currentPhase) 1f else 0f,
                    animationSpec = tween(400),
                    label = "progress_$i"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BackgroundElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(
                                Brush.horizontalGradient(listOf(PurpleCore, GoldCore))
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingNavBar(
    phase: Int,
    canProceed: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundSurface)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (phase > 0) {
            OutlinedButton(
                onClick = onBack,
                border = BorderStroke(1.dp, BorderDefault),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("BACK", style = AriseTypography.labelMedium)
            }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        Button(
            onClick = onNext,
            enabled = canProceed && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleCore,
                disabledContainerColor = PurpleDim
            ),
            modifier = Modifier.glowEffect(if (canProceed) PurpleGlow else Color.Transparent)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (phase < 5) "NEXT ▶" else "ARISE ▲",
                    style = AriseTypography.labelLarge.copy(color = TextPrimary)
                )
            }
        }
    }
}

// ── Phase 0: Awakening (name + epithets) ──────────────────────────────────────

@Composable
fun Phase0Awakening(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "HUNTER REGISTRATION\nPROTOCOL",
            style = AriseTypography.headlineMedium.copy(letterSpacing = 2.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "[ The System has detected an unregistered individual. Provide your designation. ]",
            style = SystemTextStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.answers.hunterName,
            onValueChange = vm::setHunterName,
            label = { Text("HUNTER NAME", style = AriseTypography.labelSmall.copy(color = PurpleLight)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleCore,
                unfocusedBorderColor = BorderDefault,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PurpleCore,
                focusedContainerColor = BackgroundCard,
                unfocusedContainerColor = BackgroundCard
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "SELECT YOUR EPITHET — Choose 3 words that define you",
            style = AriseTypography.labelSmall.copy(color = TextSecondary)
        )
        Text(
            "${state.selectedEpithets.size}/3 selected",
            style = AriseTypography.labelSmall.copy(color = PurpleLight)
        )

        // Word grid
        FlowRowLayout {
            EPITHET_WORDS.forEach { word ->
                val selected = word in state.selectedEpithets
                val canSelect = selected || state.selectedEpithets.size < 3
                EpithetChip(
                    word = word,
                    selected = selected,
                    enabled = canSelect,
                    onClick = { vm.toggleEpithetWord(word) }
                )
            }
        }

        if (state.selectedEpithets.size == 3) {
            Text(
                "\"${state.selectedEpithets.joinToString(" ")}\"",
                style = AriseTypography.titleMedium.copy(color = GoldCore),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EpithetChip(word: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (selected) PurpleCore else BackgroundCard
    val border = if (selected) PurpleCore else BorderDefault
    val textColor = if (selected) TextPrimary else if (enabled) TextSecondary else TextDim

    Box(
        modifier = Modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg.copy(alpha = if (selected) 1f else 0.5f))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(word, style = AriseTypography.labelSmall.copy(color = textColor, fontSize = 12.sp))
    }
}

// Simple flow row using a custom layout (no accompanist dependency)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowLayout(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
        content = { content() }
    )
}

// ── Phase 1: Goals ────────────────────────────────────────────────────────────

@Composable
fun Phase1Goals(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("WHAT DO YOU WANT TO ACHIEVE?", style = AriseTypography.headlineSmall)
        Text(
            "[ Select up to 4 goals. The System will tailor your missions accordingly. ]",
            style = SystemTextStyle
        )
        Text(
            "${state.answers.goals.size}/4 selected",
            style = AriseTypography.labelSmall.copy(color = PurpleLight)
        )

        Goal.entries.forEach { goal ->
            val selected = goal in state.answers.goals
            val enabled = selected || state.answers.goals.size < 4
            GoalCard(goal = goal, selected = selected, enabled = enabled) {
                vm.toggleGoal(goal)
            }
        }
    }
}

@Composable
fun GoalCard(goal: Goal, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val border = if (selected) PurpleCore else BorderDefault
    val bg = if (selected) PurpleFaint else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(12.dp))
                .background(bg)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                goal.displayName,
                style = AriseTypography.bodyMedium.copy(
                    color = if (enabled) TextPrimary else TextDim
                )
            )
            if (selected) {
                Icon(Icons.Filled.CheckCircle, null, tint = PurpleCore, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Filled.RadioButtonUnchecked, null, tint = TextDim, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── Phase 2: Status Assessment ─────────────────────────────────────────────────

@Composable
fun Phase2Status(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("CURRENT STATUS ASSESSMENT", style = AriseTypography.headlineSmall)
        Text("[ The System analyses your baseline. Be honest. ]", style = SystemTextStyle)

        // Fitness level
        LabelSection("FITNESS LEVEL")
        FitnessLevel.entries.forEach { level ->
            SelectionRow(
                label = level.displayName,
                selected = state.answers.fitnessLevel == level,
                onClick = { vm.setFitnessLevel(level) }
            )
        }

        // Sleep quality
        LabelSection("SLEEP QUALITY")
        SleepQuality.entries.forEach { q ->
            SelectionRow(q.displayName, state.answers.sleepQuality == q) { vm.setSleepQuality(q) }
        }

        // Stress level
        LabelSection("STRESS LEVEL")
        StressLevel.entries.forEach { s ->
            SelectionRow(s.displayName, state.answers.stressLevel == s) { vm.setStressLevel(s) }
        }

        // Available time
        LabelSection("AVAILABLE TIME FOR SELF-IMPROVEMENT")
        AvailableTime.entries.forEach { t ->
            SelectionRow(t.displayName, state.answers.availableTime == t) { vm.setAvailableTime(t) }
        }
    }
}

// ── Phase 3: Personality & Mindset ───────────────────────────────────────────

@Composable
fun Phase3Personality(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("MINDSET SCAN", style = AriseTypography.headlineSmall)
        Text("[ The System calibrates your psychological profile. ]", style = SystemTextStyle)

        BinarySelector(
            question = "YOUR DRIVE STYLE",
            optionA = "Competitive — I am driven by rankings and comparison",
            optionB = "Intrinsic — I improve for myself, not others",
            selectedA = state.answers.competitiveStyle,
            onSelectA = { vm.setCompetitiveStyle(true) },
            onSelectB = { vm.setCompetitiveStyle(false) }
        )

        BinarySelector(
            question = "ROUTINE PREFERENCE",
            optionA = "Routine — I like knowing exactly what I'll do each day",
            optionB = "Variety — I need different challenges to stay engaged",
            selectedA = state.answers.prefersRoutine,
            onSelectA = { vm.setPrefersRoutine(true) },
            onSelectB = { vm.setPrefersRoutine(false) }
        )

        LabelSection("FAILURE RESPONSE")
        FailureResponse.entries.forEach { f ->
            SelectionRow(f.displayName, state.answers.failureResponse == f) { vm.setFailureResponse(f) }
        }

        LabelSection("ACCOUNTABILITY STYLE")
        AccountabilityStyle.entries.forEach { a ->
            SelectionRow(a.displayName, state.answers.accountabilityStyle == a) { vm.setAccountabilityStyle(a) }
        }
    }
}

// ── Phase 4: History ─────────────────────────────────────────────────────────

@Composable
fun Phase4History(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("HUNTER RECORD SCAN", style = AriseTypography.headlineSmall)
        Text("[ The System examines your past to calibrate your future. ]", style = SystemTextStyle)

        BinarySelector(
            question = "HAVE YOU TRIED HABIT SYSTEMS BEFORE?",
            optionA = "Yes — I've attempted this before",
            optionB = "No — This is my first time",
            selectedA = state.answers.triedBefore,
            onSelectA = { vm.setTriedBefore(true) },
            onSelectB = { vm.setTriedBefore(false) }
        )

        LabelSection("LONGEST STREAK ACHIEVED")
        LongestStreak.entries.forEach { s ->
            SelectionRow(s.displayName, state.answers.longestStreak == s) { vm.setLongestStreak(s) }
        }

        if (state.answers.triedBefore) {
            LabelSection("WHAT KILLED PREVIOUS ATTEMPTS? (Select all that apply)")
            FailureReason.entries.forEach { r ->
                val checked = r in state.answers.failureReasons
                MultiSelectRow(r.displayName, checked) { vm.toggleFailureReason(r) }
            }
        }
    }
}

// ── Phase 5: Schedule ─────────────────────────────────────────────────────────

@Composable
fun Phase5Schedule(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("SYSTEM CONFIGURATION", style = AriseTypography.headlineSmall)
        Text("[ Final calibration. Set your rest day and notification preferences. ]", style = SystemTextStyle)

        LabelSection("DESIGNATED REST DAY")
        DayOfWeek.entries.forEach { day ->
            SelectionRow(day.name, state.answers.restDay == day) { vm.setRestDay(day) }
        }

        LabelSection("NOTIFICATION TIME (HOUR)")
        Text(
            "Daily reminder at: ${state.answers.notificationHour}:00",
            style = AriseTypography.bodyMedium.copy(color = TextPrimary)
        )
        Slider(
            value = state.answers.notificationHour.toFloat(),
            onValueChange = { vm.setNotificationHour(it.toInt()) },
            valueRange = 5f..22f,
            steps = 16,
            colors = SliderDefaults.colors(
                thumbColor = PurpleCore,
                activeTrackColor = PurpleCore,
                inactiveTrackColor = BorderDefault
            )
        )

        LabelSection("STARTING DIFFICULTY")
        StartingDifficulty.entries.forEach { d ->
            SelectionRow(d.displayName, state.answers.startingDifficulty == d) { vm.setStartingDifficulty(d) }
        }
    }
}

// ── Reusable sub-components ───────────────────────────────────────────────────

@Composable
fun LabelSection(text: String) {
    Text(text, style = AriseTypography.labelMedium.copy(color = PurpleLight, letterSpacing = 1.5.sp))
}

@Composable
fun SelectionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) PurpleCore else BorderDefault
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PurpleFaint else Color.Transparent)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AriseTypography.bodyMedium.copy(color = if (selected) TextPrimary else TextSecondary))
        if (selected) {
            Icon(Icons.Filled.RadioButtonChecked, null, tint = PurpleCore, modifier = Modifier.size(18.dp))
        } else {
            Icon(Icons.Filled.RadioButtonUnchecked, null, tint = TextDim, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun MultiSelectRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) PurpleFaint else Color.Transparent)
            .border(1.dp, if (checked) PurpleCore else BorderDefault, RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AriseTypography.bodyMedium.copy(color = if (checked) TextPrimary else TextSecondary))
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = PurpleCore,
                uncheckedColor = TextDim,
                checkmarkColor = TextPrimary
            )
        )
    }
}

@Composable
fun BinarySelector(
    question: String,
    optionA: String,
    optionB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelSection(question)
        SelectionRow(optionA, selectedA, onSelectA)
        SelectionRow(optionB, !selectedA, onSelectB)
    }
}
