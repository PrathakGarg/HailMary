package com.arise.habitquest.presentation.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class MissionBoardUiState(
    val dailyMissions: List<Mission> = emptyList(),
    val weeklyMissions: List<Mission> = emptyList(),
    val bossRaids: List<Mission> = emptyList(),
    val penaltyZone: List<Mission> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class MissionBoardViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionBoardUiState())
    val uiState: StateFlow<MissionBoardUiState> = _uiState.asStateFlow()

    init {
        val today = timeProvider.sessionDay()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)

        viewModelScope.launch {
            combine(
                missionRepository.observeMissionsForDate(today),
                missionRepository.observeWeeklyMissions(weekStart, weekEnd),
                missionRepository.observeBossRaids(),
                missionRepository.observePenaltyZone()
            ) { daily, weekly, boss, penalty ->
                MissionBoardUiState(
                    dailyMissions = daily.filter { it.type == MissionType.DAILY },
                    weeklyMissions = weekly,
                    bossRaids = boss,
                    penaltyZone = penalty,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.update { current ->
                    newState.copy(selectedTab = current.selectedTab)
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
