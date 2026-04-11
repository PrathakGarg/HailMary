package com.arise.habitquest.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.CompleteMissionUseCase
import com.arise.habitquest.domain.usecase.CompletionResult
import com.arise.habitquest.domain.usecase.FailMissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val profile: UserProfile? = null,
    val todayMissions: List<Mission> = emptyList(),
    val systemMessage: String = "",
    val lastCompletionResult: CompletionResult? = null,
    val showSystemNotification: Boolean = false,
    val notificationMessage: String = "",
    val pendingRankUp: String = "",
    val currentDate: LocalDate = LocalDate.now(),
    val minutesUntilReset: Long = Long.MAX_VALUE,
    val isLoading: Boolean = true
) {
    val completedCount: Int get() = todayMissions.count { it.isCompleted }
    val totalCount: Int get() = todayMissions.size
    val completionPercent: Float get() = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val completeMission: CompleteMissionUseCase,
    private val failMission: FailMissionUseCase,
    private val dataStore: OnboardingDataStore,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        observePendingRankUp()
        observeSessionDay()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepository.observeUserProfile().collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    // Re-check the session day every minute. Switches the mission subscription
    // automatically when the day rolls over at the configured reset time.
    private fun observeSessionDay() {
        viewModelScope.launch {
            var currentDate: LocalDate? = null
            var missionJob: kotlinx.coroutines.Job? = null
            while (true) {
                val sessionDay = timeProvider.sessionDay()
                val mins = timeProvider.minutesUntilReset()
                _uiState.update { it.copy(minutesUntilReset = mins) }
                if (sessionDay != currentDate) {
                    currentDate = sessionDay
                    _uiState.update { it.copy(currentDate = sessionDay) }
                    missionJob?.cancel()
                    missionJob = launch {
                        missionRepository.observeMissionsForDate(sessionDay).collect { missions ->
                            // Only show DAILY missions on the home screen; boss/weekly/penalty
                            // have their own dedicated tabs on the Mission Board.
                            _uiState.update { it.copy(todayMissions = missions.filter { m -> m.type == MissionType.DAILY }) }
                        }
                    }
                }
                delay(60_000L) // check once per minute
            }
        }
    }

    private fun observePendingRankUp() {
        viewModelScope.launch {
            dataStore.pendingRankUp.collect { rankName ->
                _uiState.update { it.copy(pendingRankUp = rankName) }
            }
        }
    }

    fun clearPendingRankUp() {
        viewModelScope.launch {
            dataStore.setPendingRankUp("")
        }
    }

    fun quickCompleteMission(mission: Mission) {
        viewModelScope.launch {
            val profile = _uiState.value.profile ?: return@launch
            val result = completeMission(mission, profile)
            _uiState.update {
                it.copy(
                    lastCompletionResult = result,
                    showSystemNotification = true,
                    notificationMessage = buildCompletionMessage(result)
                )
            }
        }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(showSystemNotification = false) }
    }

    private fun buildCompletionMessage(result: CompletionResult): String {
        val parts = mutableListOf("Gate cleared. +${result.xpGained} XP")
        if (result.levelUpResult != null) parts.add("LEVEL UP → ${result.levelUpResult.newLevel}")
        if (result.promotedToShadow) parts.add("SHADOW PROMOTED")
        return parts.joinToString("  |  ")
    }
}
