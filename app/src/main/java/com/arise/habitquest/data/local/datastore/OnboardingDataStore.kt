package com.arise.habitquest.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arise_prefs")

@Singleton
class OnboardingDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val HUNTER_NAME = stringPreferencesKey("hunter_name")
        val NOTIFICATION_GRANTED = booleanPreferencesKey("notification_granted")
        val DAILY_RESET_LAST_DATE = stringPreferencesKey("daily_reset_last_date")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val THEME_INITIALIZED = booleanPreferencesKey("theme_initialized")
        val FOCUS_THEMES = stringPreferencesKey("focus_themes")        // comma-separated FocusTheme names
        val PENDING_RANK_UP = stringPreferencesKey("pending_rank_up")  // rank name or "" if none
        val LAST_MONTHLY_REPORT = stringPreferencesKey("last_monthly_report") // "YYYY-MM" of last sent
        val DAY_START_MINUTES = intPreferencesKey("day_start_minutes")         // minute-of-day, 30-min granularity
        val DAY_START_HOUR = intPreferencesKey("day_start_hour")               // legacy key fallback
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETE] ?: false }

    val hunterName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.HUNTER_NAME] ?: "" }

    val lastDailyResetDate: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.DAILY_RESET_LAST_DATE] ?: "" }

    val notificationHour: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[Keys.NOTIFICATION_HOUR] ?: 8 }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setHunterName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HUNTER_NAME] = name
        }
    }

    suspend fun setLastDailyResetDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_RESET_LAST_DATE] = date
        }
    }

    suspend fun setNotificationHour(hour: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_HOUR] = hour
        }
    }

    suspend fun setNotificationGranted(granted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_GRANTED] = granted
        }
    }

    val focusThemes: kotlinx.coroutines.flow.Flow<Set<String>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[Keys.FOCUS_THEMES] ?: ""
            if (raw.isBlank()) emptySet() else raw.split(",").toSet()
        }

    suspend fun setFocusThemes(themes: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_THEMES] = themes.joinToString(",")
        }
    }

    val pendingRankUp: kotlinx.coroutines.flow.Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.PENDING_RANK_UP] ?: "" }

    suspend fun setPendingRankUp(rankName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PENDING_RANK_UP] = rankName
        }
    }

    val lastMonthlyReport: kotlinx.coroutines.flow.Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.LAST_MONTHLY_REPORT] ?: "" }

    suspend fun setLastMonthlyReport(yearMonth: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_MONTHLY_REPORT] = yearMonth
        }
    }

    val dayStartMinutes: Flow<Int> = context.dataStore.data
        .map { prefs ->
            // Backward compatibility for existing installs that only have the legacy hour key.
            val minutes = prefs[Keys.DAY_START_MINUTES]
                ?: (prefs[Keys.DAY_START_HOUR] ?: 4) * 60
            (minutes / 30).coerceIn(0, 47) * 30
        }

    suspend fun setDayStartMinutes(minutes: Int) {
        val normalized = (minutes / 30).coerceIn(0, 47) * 30
        context.dataStore.edit { prefs ->
            prefs[Keys.DAY_START_MINUTES] = normalized
            // Keep legacy key in sync for any older call-sites not yet migrated.
            prefs[Keys.DAY_START_HOUR] = normalized / 60
        }
    }
}
