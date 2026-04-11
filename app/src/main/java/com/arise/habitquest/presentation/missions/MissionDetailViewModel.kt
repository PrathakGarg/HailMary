package com.arise.habitquest.presentation.missions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MissionDetailUiState(
    val mission: Mission? = null,
    val profile: UserProfile? = null,
    val useMiniVersion: Boolean = false,
    val isLoading: Boolean = true,
    val completionResult: CompletionResult? = null,
    val failResult: FailResult? = null,
    val actionCompleted: Boolean = false
)

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val completeMission: CompleteMissionUseCase,
    private val failMission: FailMissionUseCase
) : ViewModel() {

    private val missionId: String = checkNotNull(savedStateHandle["missionId"])

    private val _uiState = MutableStateFlow(MissionDetailUiState())
    val uiState: StateFlow<MissionDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val mission = missionRepository.getMissionById(missionId)
            val profile = userRepository.getUserProfile()
            _uiState.update { it.copy(mission = mission, profile = profile, isLoading = false) }
        }
    }

    fun toggleMiniVersion() {
        _uiState.update { it.copy(useMiniVersion = !it.useMiniVersion) }
    }

    fun complete() {
        viewModelScope.launch {
            val mission = _uiState.value.mission ?: return@launch
            val profile = _uiState.value.profile ?: return@launch
            val result = completeMission(mission, profile, _uiState.value.useMiniVersion)
            _uiState.update { it.copy(completionResult = result, actionCompleted = true) }
        }
    }

    fun fail() {
        viewModelScope.launch {
            val mission = _uiState.value.mission ?: return@launch
            val profile = _uiState.value.profile ?: return@launch
            val result = failMission(mission, profile)
            _uiState.update { it.copy(failResult = result, actionCompleted = true) }
        }
    }
}
