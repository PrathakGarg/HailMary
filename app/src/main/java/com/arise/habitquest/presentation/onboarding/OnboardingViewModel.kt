package com.arise.habitquest.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.domain.model.*
import com.arise.habitquest.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

data class OnboardingUiState(
    val phase: Int = 0,          // 0–5 (6 phases)
    val answers: OnboardingAnswers = OnboardingAnswers(),
    val selectedEpithets: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val canProceed: Boolean get() = when (phase) {
        0 -> answers.hunterName.isNotBlank() && selectedEpithets.size == 3
        1 -> answers.goals.isNotEmpty()
        2 -> true // sliders always have values
        3 -> true // toggles always have values
        4 -> true // optional checkboxes
        5 -> true // schedule
        else -> false
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setHunterName(name: String) {
        _uiState.update { it.copy(answers = it.answers.copy(hunterName = name)) }
    }

    fun toggleEpithetWord(word: String) {
        _uiState.update { state ->
            val current = state.selectedEpithets.toMutableList()
            if (word in current) current.remove(word)
            else if (current.size < 3) current.add(word)
            state.copy(
                selectedEpithets = current,
                answers = state.answers.copy(epithets = current)
            )
        }
    }

    fun toggleGoal(goal: Goal) {
        _uiState.update { state ->
            val current = state.answers.goals.toMutableSet()
            if (goal in current) current.remove(goal)
            else if (current.size < 4) current.add(goal)
            state.copy(answers = state.answers.copy(goals = current))
        }
    }

    fun setFitnessLevel(level: FitnessLevel) =
        _uiState.update { it.copy(answers = it.answers.copy(fitnessLevel = level)) }

    fun setSleepQuality(q: SleepQuality) =
        _uiState.update { it.copy(answers = it.answers.copy(sleepQuality = q)) }

    fun setStressLevel(s: StressLevel) =
        _uiState.update { it.copy(answers = it.answers.copy(stressLevel = s)) }

    fun setWorkHours(h: Int) =
        _uiState.update { it.copy(answers = it.answers.copy(workHoursPerDay = h)) }

    fun setAvailableTime(t: AvailableTime) =
        _uiState.update { it.copy(answers = it.answers.copy(availableTime = t)) }

    fun setCompetitiveStyle(c: Boolean) =
        _uiState.update { it.copy(answers = it.answers.copy(competitiveStyle = c)) }

    fun setPrefersRoutine(r: Boolean) =
        _uiState.update { it.copy(answers = it.answers.copy(prefersRoutine = r)) }

    fun setFailureResponse(f: FailureResponse) =
        _uiState.update { it.copy(answers = it.answers.copy(failureResponse = f)) }

    fun setAccountabilityStyle(a: AccountabilityStyle) =
        _uiState.update { it.copy(answers = it.answers.copy(accountabilityStyle = a)) }

    fun setTriedBefore(b: Boolean) =
        _uiState.update { it.copy(answers = it.answers.copy(triedBefore = b)) }

    fun setLongestStreak(s: LongestStreak) =
        _uiState.update { it.copy(answers = it.answers.copy(longestStreak = s)) }

    fun toggleFailureReason(r: FailureReason) {
        _uiState.update { state ->
            val current = state.answers.failureReasons.toMutableSet()
            if (r in current) current.remove(r) else current.add(r)
            state.copy(answers = state.answers.copy(failureReasons = current))
        }
    }

    fun setRestDay(day: DayOfWeek) =
        _uiState.update { it.copy(answers = it.answers.copy(restDay = day)) }

    fun setNotificationHour(h: Int) =
        _uiState.update { it.copy(answers = it.answers.copy(notificationHour = h)) }

    fun setStartingDifficulty(d: StartingDifficulty) =
        _uiState.update { it.copy(answers = it.answers.copy(startingDifficulty = d)) }

    fun nextPhase() {
        _uiState.update {
            if (it.phase < 5) it.copy(phase = it.phase + 1) else it
        }
    }

    fun previousPhase() {
        _uiState.update { if (it.phase > 0) it.copy(phase = it.phase - 1) else it }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                completeOnboarding(_uiState.value.answers)
                _uiState.update { it.copy(isLoading = false, isComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
