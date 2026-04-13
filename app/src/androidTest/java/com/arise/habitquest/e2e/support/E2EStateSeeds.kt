package com.arise.habitquest.e2e.support

import android.content.Context
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import kotlinx.coroutines.runBlocking

object E2EStateSeeds {

    fun seedReturningUser(context: Context) {
        runBlocking {
            val dataStore = OnboardingDataStore(context)
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
        }
    }

    fun seedFirstRunUser(context: Context) {
        runBlocking {
            val dataStore = OnboardingDataStore(context)
            dataStore.setOnboardingComplete(false)
            dataStore.setHunterName("")
        }
    }
}
