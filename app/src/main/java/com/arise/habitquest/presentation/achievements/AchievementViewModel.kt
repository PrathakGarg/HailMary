package com.arise.habitquest.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.Rarity
import com.arise.habitquest.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementUiState(
    val achievements: List<Achievement> = emptyList(),
    val selectedFilter: AchievementFilter = AchievementFilter.ALL
) {
    val filteredAchievements: List<Achievement> get() = when (selectedFilter) {
        AchievementFilter.ALL -> achievements
        AchievementFilter.UNLOCKED -> achievements.filter { it.isUnlocked }
        AchievementFilter.COMMON -> achievements.filter { it.rarity == Rarity.COMMON }
        AchievementFilter.RARE -> achievements.filter { it.rarity == Rarity.RARE }
        AchievementFilter.EPIC -> achievements.filter { it.rarity == Rarity.EPIC }
        AchievementFilter.LEGENDARY -> achievements.filter { it.rarity == Rarity.LEGENDARY || it.rarity == Rarity.MYTHIC }
    }

    val unlockedCount: Int get() = achievements.count { it.isUnlocked }
    val totalCount: Int get() = achievements.size
}

enum class AchievementFilter(val label: String) {
    ALL("All"), UNLOCKED("Unlocked"),
    COMMON("Common"), RARE("Rare"), EPIC("Epic"), LEGENDARY("Legendary")
}

@HiltViewModel
class AchievementViewModel @Inject constructor(
    achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            achievementRepository.observeAllAchievements().collect { list ->
                _uiState.update { it.copy(achievements = list) }
            }
        }
    }

    fun setFilter(filter: AchievementFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
