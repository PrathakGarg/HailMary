package com.arise.habitquest.presentation.missions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.data.local.database.dao.MissionTrackingDao
import com.arise.habitquest.data.local.database.entity.MissionTrackingLogEntity
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
    val trackingEnabled: Boolean = false,
    val trackingPrimaryLabel: String = "Metric",
    val trackingPrimaryHint: String = "Enter metric value",
    val trackingSecondaryLabel: String = "Context",
    val trackingSecondaryHint: String = "Add context",
    val trackingRequiresNumericPrimary: Boolean = false,
    val trackingPrimaryValue: String = "",
    val trackingSecondaryValue: String = "",
    val trackingNotes: String = "",
    val trackingLogs: List<MissionTrackingLogEntity> = emptyList(),
    val trackingMessage: String? = null,
    val completionResult: CompletionResult? = null,
    val failResult: FailResult? = null,
    val actionCompleted: Boolean = false,
    val deprioritizeReplaced: Boolean = false,
    val deprioritizeError: Boolean = false
)

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository,
    private val missionTrackingDao: MissionTrackingDao,
    private val userRepository: UserRepository,
    private val completeMission: CompleteMissionUseCase,
    private val failMission: FailMissionUseCase,
    private val deprioritizeMissionTemplate: DeprioritizeMissionTemplateUseCase
) : ViewModel() {

    private companion object {
        private const val TEMPLATE_SPEED_READING = "tpl_speed_reading"
        private const val TEMPLATE_HYDRATION = "tpl_hydration"
        private const val TEMPLATE_FINANCE_REVIEW = "tpl_finance_review"
        private const val TEMPLATE_INTERMITTENT_FAST = "tpl_intermittent_fast"
        private const val TEMPLATE_STEPS = "tpl_steps"
    }

    private val missionId: String = checkNotNull(savedStateHandle["missionId"])

    private val _uiState = MutableStateFlow(MissionDetailUiState())
    val uiState: StateFlow<MissionDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val mission = missionRepository.getMissionById(missionId)
            val profile = userRepository.getUserProfile()

            val trackingTemplate = mission?.let(::resolveTrackingTemplate)
            val trackingLogs = if (mission != null && trackingTemplate != null) {
                missionTrackingDao.getLogsForMission(mission.id, limit = 8)
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    mission = mission,
                    profile = profile,
                    isLoading = false,
                    trackingEnabled = trackingTemplate != null,
                    trackingPrimaryLabel = trackingTemplate?.primaryLabel ?: "Metric",
                    trackingPrimaryHint = trackingTemplate?.primaryHint ?: "Enter metric value",
                    trackingSecondaryLabel = trackingTemplate?.secondaryLabel ?: "Context",
                    trackingSecondaryHint = trackingTemplate?.secondaryHint ?: "Add context",
                    trackingRequiresNumericPrimary = trackingTemplate?.primaryMustBeNumeric ?: false,
                    trackingLogs = trackingLogs
                )
            }
        }
    }

    private data class TrackingTemplate(
        val primaryLabel: String,
        val primaryHint: String,
        val secondaryLabel: String,
        val secondaryHint: String,
        val primaryMustBeNumeric: Boolean
    )

    private val trackingTemplatesByTemplateId = mapOf(
        TEMPLATE_SPEED_READING to TrackingTemplate(
            primaryLabel = "Reading Speed (WPM)",
            primaryHint = "Example: 280",
            secondaryLabel = "Article/Book Read",
            secondaryHint = "Title or source",
            primaryMustBeNumeric = true
        ),
        TEMPLATE_HYDRATION to TrackingTemplate(
            primaryLabel = "Glasses Completed",
            primaryHint = "Example: 8",
            secondaryLabel = "Total Water (ml)",
            secondaryHint = "Optional, e.g. 2000",
            primaryMustBeNumeric = true
        ),
        TEMPLATE_FINANCE_REVIEW to TrackingTemplate(
            primaryLabel = "Transactions Logged",
            primaryHint = "Example: 4",
            secondaryLabel = "Total Spent",
            secondaryHint = "Optional, e.g. 126.40",
            primaryMustBeNumeric = true
        ),
        TEMPLATE_INTERMITTENT_FAST to TrackingTemplate(
            primaryLabel = "Fasting Window (Hours)",
            primaryHint = "Example: 16",
            secondaryLabel = "Window",
            secondaryHint = "Start-End, e.g. 20:00-12:00",
            primaryMustBeNumeric = true
        ),
        TEMPLATE_STEPS to TrackingTemplate(
            primaryLabel = "Steps Completed",
            primaryHint = "Example: 9200",
            secondaryLabel = "Tracker Source",
            secondaryHint = "Phone, watch, etc.",
            primaryMustBeNumeric = true
        )
    )

    // Keep legacy title aliases for already-generated missions that may have null parentTemplateId.
    private val trackingTemplatesByTitleAlias = mapOf(
        "accelerated tome" to TEMPLATE_SPEED_READING,
        "life source" to TEMPLATE_HYDRATION,
        "field scout" to TEMPLATE_STEPS,
        "budget sentinel" to TEMPLATE_FINANCE_REVIEW,
        "hunger protocol" to TEMPLATE_INTERMITTENT_FAST
    )

    private fun resolveTrackingTemplate(mission: Mission): TrackingTemplate? {
        val templateId = mission.parentTemplateId
        if (!templateId.isNullOrBlank()) {
            trackingTemplatesByTemplateId[templateId]?.let { return it }
        }

        val loweredTitle = mission.title.lowercase()
        val aliasedTemplateId = trackingTemplatesByTitleAlias
            .entries
            .firstOrNull { loweredTitle.contains(it.key) }
            ?.value
            ?: return null

        return trackingTemplatesByTemplateId[aliasedTemplateId]
    }

    fun toggleMiniVersion() {
        _uiState.update { it.copy(useMiniVersion = !it.useMiniVersion) }
    }

    fun onTrackingPrimaryChanged(value: String) {
        _uiState.update { it.copy(trackingPrimaryValue = value) }
    }

    fun onTrackingSecondaryChanged(value: String) {
        _uiState.update { it.copy(trackingSecondaryValue = value) }
    }

    fun onTrackingNotesChanged(value: String) {
        _uiState.update { it.copy(trackingNotes = value) }
    }

    fun saveTrackingLog() {
        viewModelScope.launch {
            val state = _uiState.value
            val mission = state.mission ?: return@launch
            if (!state.trackingEnabled) return@launch

            val primary = state.trackingPrimaryValue.trim()
            if (primary.isBlank()) {
                _uiState.update { it.copy(trackingMessage = "Enter ${state.trackingPrimaryLabel.lowercase()} first.") }
                return@launch
            }

            if (state.trackingRequiresNumericPrimary && primary.toFloatOrNull() == null) {
                _uiState.update { it.copy(trackingMessage = "${state.trackingPrimaryLabel} must be a number.") }
                return@launch
            }

            missionTrackingDao.insertLog(
                MissionTrackingLogEntity(
                    missionId = mission.id,
                    missionTitle = mission.title,
                    missionDueDate = mission.dueDate.toString(),
                    primaryLabel = state.trackingPrimaryLabel,
                    primaryValue = primary,
                    secondaryLabel = state.trackingSecondaryLabel,
                    secondaryValue = state.trackingSecondaryValue.trim(),
                    notes = state.trackingNotes.trim()
                )
            )

            val refreshedLogs = missionTrackingDao.getLogsForMission(mission.id, limit = 8)
            _uiState.update {
                it.copy(
                    trackingPrimaryValue = "",
                    trackingSecondaryValue = "",
                    trackingNotes = "",
                    trackingLogs = refreshedLogs,
                    trackingMessage = "Tracked data saved."
                )
            }
        }
    }

    fun consumeTrackingMessage() {
        _uiState.update { it.copy(trackingMessage = null) }
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

    fun deprioritizeAndReplace() {
        viewModelScope.launch {
            val mission = _uiState.value.mission ?: return@launch
            val replaced = deprioritizeMissionTemplate(mission.id)
            if (replaced) {
                _uiState.update { it.copy(deprioritizeReplaced = true, deprioritizeError = false) }
            } else {
                _uiState.update { it.copy(deprioritizeError = true) }
            }
        }
    }
}
