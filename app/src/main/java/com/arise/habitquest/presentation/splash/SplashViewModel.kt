package com.arise.habitquest.presentation.splash

import androidx.lifecycle.ViewModel
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.UnlockAchievementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val dataStore: OnboardingDataStore,
    private val userRepository: UserRepository,
    private val unlockAchievement: UnlockAchievementUseCase
) : ViewModel() {

    suspend fun checkAndNavigate(
        onNavigateToOnboarding: () -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        delay(2800) // Show splash for at least 2.8s
        val onboardingComplete = dataStore.onboardingComplete.first()

        if (onboardingComplete && !dataStore.achievementRepairV1Done.first()) {
            runCatching {
                val profile = userRepository.getUserProfile()
                if (profile != null) {
                    unlockAchievement.reconcileInvalidUnlocks(profile)
                }
                dataStore.setAchievementRepairV1Done(true)
            }
        }

        withContext(Dispatchers.Main.immediate) {
            if (onboardingComplete) {
                onNavigateToHome()
            } else {
                onNavigateToOnboarding()
            }
        }
    }
}
