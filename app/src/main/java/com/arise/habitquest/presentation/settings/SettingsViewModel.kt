package com.arise.habitquest.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.FocusTheme
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.usecase.RegenerateCurrentMissionsUseCase
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.worker.DailyResetWorker
import com.arise.habitquest.worker.MorningNotificationWorker
import com.arise.habitquest.worker.PreResetReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val profile: UserProfile? = null,
    val notificationHour: Int = 8,
    val dayStartMinutes: Int = 270,
    val activeFocusThemes: Set<FocusTheme> = setOf(FocusTheme.PHYSICAL_PERFORMANCE, FocusTheme.MENTAL_CLARITY),
    val excludeInboxMissions: Boolean = true,
    val deprioritizedTemplateIds: Set<String> = emptySet(),
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val dataStore: OnboardingDataStore,
    private val timeProvider: TimeProvider,
    private val regenerateCurrentMissions: RegenerateCurrentMissionsUseCase
) : ViewModel() {

    private data class SettingsCoreState(
        val profile: UserProfile?,
        val notificationHour: Int,
        val dayStartMinutes: Int,
        val activeFocusThemes: Set<FocusTheme>,
        val excludeInboxMissions: Boolean
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val coreFlow = combine(
                userRepository.observeUserProfile(),
                dataStore.notificationHour,
                dataStore.dayStartMinutes,
                dataStore.focusThemes,
                dataStore.excludeInboxMissions
            ) { profile, notifHour, dayStartMinutes, themeNames, excludeInboxMissions ->
                val themes = themeNames.mapNotNull { name ->
                    FocusTheme.entries.find { it.name == name }
                }.toSet().ifEmpty { setOf(FocusTheme.PHYSICAL_PERFORMANCE, FocusTheme.MENTAL_CLARITY) }
                SettingsCoreState(
                    profile = profile,
                    notificationHour = notifHour,
                    dayStartMinutes = dayStartMinutes,
                    activeFocusThemes = themes,
                    excludeInboxMissions = excludeInboxMissions
                )
            }

            combine(coreFlow, dataStore.deprioritizedTemplateIds) { core, deprioritizedTemplateIds ->
                SettingsUiState(
                    profile = core.profile,
                    notificationHour = core.notificationHour,
                    dayStartMinutes = core.dayStartMinutes,
                    activeFocusThemes = core.activeFocusThemes,
                    excludeInboxMissions = core.excludeInboxMissions,
                    deprioritizedTemplateIds = deprioritizedTemplateIds
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setRestDay(day: Int) {
        viewModelScope.launch {
            userRepository.updateRestDay(day)
        }
    }

    fun regenerateMissions() {
        viewModelScope.launch {
            regenerateCurrentMissions()
        }
    }

    fun setDayStartMinutes(minutes: Int) {
        viewModelScope.launch {
            dataStore.setDayStartMinutes(minutes)
            timeProvider.setDayStartMinutes(minutes)
            val wm = WorkManager.getInstance(context)
            DailyResetWorker.schedule(wm, timeProvider)
            PreResetReminderWorker.schedule(wm, timeProvider)
        }
    }

    fun setNotificationHour(hour: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(notificationHour = hour) }
            dataStore.setNotificationHour(hour)
            userRepository.updateNotificationHour(hour)
            // Reschedule the morning worker so the new time takes effect immediately.
            MorningNotificationWorker.schedule(WorkManager.getInstance(context), timeProvider, hour)
        }
    }


    fun toggleFocusTheme(theme: FocusTheme) {
        viewModelScope.launch {
            val current = _uiState.value.activeFocusThemes.toMutableSet()
            if (theme in current) {
                if (current.size > 1) current.remove(theme) // keep at least 1 active
            } else {
                if (current.size < 3) current.add(theme) // max 3 active
            }
            dataStore.setFocusThemes(current.map { it.name }.toSet())
        }
    }

    fun activateEmergencyGrace() {
        viewModelScope.launch {
            val profile = _uiState.value.profile ?: return@launch
            if (profile.graceUsesRemaining > 0) {
                userRepository.upsertProfile(
                    profile.copy(
                        graceUsesRemaining = profile.graceUsesRemaining - 1,
                        consecutiveMissDays = 0,
                        pendingWarning = false
                    )
                )
            }
        }
    }

    fun setExcludeInboxMissions(exclude: Boolean) {
        viewModelScope.launch {
            dataStore.setExcludeInboxMissions(exclude)
        }
    }

    fun clearDeprioritizedTemplates() {
        viewModelScope.launch {
            dataStore.setDeprioritizedTemplateIds(emptySet())
        }
    }

    fun removeDeprioritizedTemplate(templateId: String) {
        viewModelScope.launch {
            val updated = _uiState.value.deprioritizedTemplateIds - templateId
            dataStore.setDeprioritizedTemplateIds(updated)
        }
    }
}
