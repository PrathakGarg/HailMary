package com.arise.habitquest.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arise.habitquest.domain.model.MissionRollbackEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val EXCLUDE_INBOX_MISSIONS = booleanPreferencesKey("exclude_inbox_missions")
        val DEPRIORITIZED_TEMPLATE_IDS = stringPreferencesKey("deprioritized_template_ids")
        val MISSION_ROLLBACK_LEDGER = stringPreferencesKey("mission_rollback_ledger")
        val ACHIEVEMENT_REPAIR_V1_DONE = booleanPreferencesKey("achievement_repair_v1_done")
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val rollbackMaxEntries = 500
    private val rollbackMaxAgeMillis = 21L * 24 * 60 * 60 * 1000

    val onboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETE] ?: false }

    val hunterName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.HUNTER_NAME] ?: "" }

    val lastDailyResetDate: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.DAILY_RESET_LAST_DATE] ?: "" }

    val notificationHour: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[Keys.NOTIFICATION_HOUR] ?: 8 }

    val achievementRepairV1Done: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.ACHIEVEMENT_REPAIR_V1_DONE] ?: false }

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

    suspend fun setAchievementRepairV1Done(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACHIEVEMENT_REPAIR_V1_DONE] = done
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

    val excludeInboxMissions: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.EXCLUDE_INBOX_MISSIONS] ?: true }

    suspend fun setExcludeInboxMissions(exclude: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EXCLUDE_INBOX_MISSIONS] = exclude
        }
    }

    val deprioritizedTemplateIds: Flow<Set<String>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[Keys.DEPRIORITIZED_TEMPLATE_IDS] ?: ""
            if (raw.isBlank()) emptySet() else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        }

    suspend fun setDeprioritizedTemplateIds(templateIds: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEPRIORITIZED_TEMPLATE_IDS] = templateIds.joinToString(",")
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

    val missionRollbackLedger: Flow<Map<String, MissionRollbackEntry>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[Keys.MISSION_ROLLBACK_LEDGER] ?: ""
            if (raw.isBlank()) {
                emptyMap()
            } else {
                runCatching {
                    json.decodeFromString<Map<String, MissionRollbackEntry>>(raw)
                }.getOrDefault(emptyMap())
            }
        }

    suspend fun setMissionRollbackEntry(missionId: String, entry: MissionRollbackEntry) {
        context.dataStore.edit { prefs ->
            val current = parseRollbackLedger(prefs[Keys.MISSION_ROLLBACK_LEDGER])
            val merged = current + (missionId to entry)
            val pruned = pruneLedgerMap(merged, System.currentTimeMillis())
            prefs[Keys.MISSION_ROLLBACK_LEDGER] = json.encodeToString(pruned)
        }
    }

    suspend fun getMissionRollbackEntry(missionId: String): MissionRollbackEntry? {
        return missionRollbackLedger.map { it[missionId] }.first()
    }

    suspend fun removeMissionRollbackEntry(missionId: String) {
        context.dataStore.edit { prefs ->
            val current = parseRollbackLedger(prefs[Keys.MISSION_ROLLBACK_LEDGER])
            prefs[Keys.MISSION_ROLLBACK_LEDGER] = json.encodeToString(current - missionId)
        }
    }

    suspend fun pruneMissionRollbackLedger(nowMillis: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            val current = parseRollbackLedger(prefs[Keys.MISSION_ROLLBACK_LEDGER])
            val pruned = pruneLedgerMap(current, nowMillis)
            prefs[Keys.MISSION_ROLLBACK_LEDGER] = json.encodeToString(pruned)
        }
    }

    suspend fun clearMissionRollbackLedger() {
        context.dataStore.edit { prefs ->
            prefs[Keys.MISSION_ROLLBACK_LEDGER] = ""
        }
    }

    private fun parseRollbackLedger(raw: String?): Map<String, MissionRollbackEntry> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, MissionRollbackEntry>>(raw)
        }.getOrDefault(emptyMap())
    }

    private fun pruneLedgerMap(
        source: Map<String, MissionRollbackEntry>,
        nowMillis: Long
    ): Map<String, MissionRollbackEntry> {
        val ageCutoff = nowMillis - rollbackMaxAgeMillis
        val ageFiltered = source
            .filterValues { it.recordedAtMillis >= ageCutoff }

        if (ageFiltered.size <= rollbackMaxEntries) return ageFiltered

        return ageFiltered.entries
            .sortedByDescending { it.value.recordedAtMillis }
            .take(rollbackMaxEntries)
            .associate { it.toPair() }
    }
}
