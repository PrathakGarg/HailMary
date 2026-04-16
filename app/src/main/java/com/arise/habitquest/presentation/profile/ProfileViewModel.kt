package com.arise.habitquest.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.data.local.database.dao.DailyLogDao
import com.arise.habitquest.data.local.database.dao.MissionDao
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.GetWeeklyMuscleCoverageUseCase
import com.arise.habitquest.presentation.history.MuscleCoverageEntry
import com.arise.habitquest.presentation.progression.ProgramDirective
import com.arise.habitquest.presentation.progression.buildProgramDirectives
import com.arise.habitquest.ui.components.CategoryStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    private val dailyLogDao: DailyLogDao,
    private val missionDao: MissionDao,
    private val weeklyCoverageUseCase: GetWeeklyMuscleCoverageUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val profileSource = userRepository.observeUserProfile()

    val profile: StateFlow<UserProfile?> = profileSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _categoryStats = MutableStateFlow<List<CategoryStats>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStats>> = _categoryStats

    private val _directives = MutableStateFlow<List<ProgramDirective>>(emptyList())
    val directives: StateFlow<List<ProgramDirective>> = _directives

    init {
        viewModelScope.launch {
            profileSource.collectLatest { currentProfile ->
                loadCategoryStats()
                loadDirectives(currentProfile)
            }
        }
    }

    private suspend fun loadCategoryStats() {
        val fmt   = DateTimeFormatter.ISO_LOCAL_DATE
        val sessionDate = timeProvider.sessionDay()
        val from  = sessionDate.minusDays(30).format(fmt)
        val to    = sessionDate.format(fmt)
        val missions = missionDao.getMissionsInRange(from, to)

        _categoryStats.value = MissionCategory.entries.map { category ->
            val cat = missions.filter { it.category == category.name }
            CategoryStats(
                category    = category,
                total       = cat.size,
                completed   = cat.count { it.isCompleted },
                failed      = cat.count { it.isFailed },
                skipped     = cat.count { it.isSkipped },
                miniAccepted = cat.count { it.acceptedMiniVersion }
            )
        }
    }

    private suspend fun loadDirectives(currentProfile: UserProfile?) {
        if (currentProfile == null) {
            _directives.value = emptyList()
            return
        }

        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val sessionDate = timeProvider.sessionDay()
        val recentLogs = dailyLogDao.getLogsInRange(
            sessionDate.minusDays(6).format(fmt),
            sessionDate.format(fmt)
        )
        val recentCompletionRate = recentLogs
            .map { it.completionRate }
            .average()
            .toFloat()
            .takeIf { !it.isNaN() }
            ?: 0f

        val weeklyCoverage = weeklyCoverageUseCase(sessionDate).regions.map { row ->
            MuscleCoverageEntry(
                region = row.region,
                assignedLoad = row.assignedLoad,
                completedLoad = row.completedLoad,
                completionRatio = if (row.assignedLoad > 0f) {
                    (row.completedLoad / row.assignedLoad).coerceIn(0f, 1f)
                } else 0f
            )
        }

        _directives.value = buildProgramDirectives(
            profile = currentProfile,
            weeklyCoverage = weeklyCoverage,
            recentCompletionRate = recentCompletionRate
        )
    }
}
