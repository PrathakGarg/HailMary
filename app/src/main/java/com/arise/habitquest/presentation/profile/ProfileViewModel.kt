package com.arise.habitquest.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.data.local.database.dao.MissionDao
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.ui.components.CategoryStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    private val missionDao: MissionDao,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userRepository
        .observeUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _categoryStats = MutableStateFlow<List<CategoryStats>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStats>> = _categoryStats

    init {
        viewModelScope.launch { loadCategoryStats() }
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
}
