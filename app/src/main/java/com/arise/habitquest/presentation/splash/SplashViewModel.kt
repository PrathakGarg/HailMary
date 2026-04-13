package com.arise.habitquest.presentation.splash

import androidx.lifecycle.ViewModel
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val dataStore: OnboardingDataStore
) : ViewModel() {

    suspend fun checkAndNavigate(
        onNavigateToOnboarding: () -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        delay(2800) // Show splash for at least 2.8s
        val onboardingComplete = dataStore.onboardingComplete.first()
        withContext(Dispatchers.Main.immediate) {
            if (onboardingComplete) {
                onNavigateToHome()
            } else {
                onNavigateToOnboarding()
            }
        }
    }
}
