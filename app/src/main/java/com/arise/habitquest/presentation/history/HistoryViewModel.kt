package com.arise.habitquest.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arise.habitquest.data.local.database.dao.DailyLogDao
import com.arise.habitquest.data.local.database.dao.MissionDao
import com.arise.habitquest.data.local.database.dao.MissionTrackingDao
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import com.arise.habitquest.data.local.database.entity.MissionTrackingLogEntity
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.MuscleRegion
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.presentation.progression.ProgramDirective
import com.arise.habitquest.presentation.progression.buildProgramDirectives
import com.arise.habitquest.domain.usecase.GetWeeklyMuscleCoverageUseCase
import com.arise.habitquest.ui.components.CategoryStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DayEntry(
    val date: LocalDate,
    val completionRate: Float,   // 0f = no data / rest day, >0 = logged
    val hasData: Boolean,
    val xpGained: Int = 0,
    val totalMissions: Int = 0,
    val wasRestDay: Boolean = false
)

data class AnalysisInsight(
    val label: String,
    val value: String,
    val subtext: String,
    val isPositive: Boolean
)

data class MuscleCoverageEntry(
    val region: MuscleRegion,
    val assignedLoad: Float,
    val completedLoad: Float,
    val completionRatio: Float
)

data class HistoryUiState(
    val calendarDays: List<DayEntry> = emptyList(),  // 91 days (13 weeks)
    val weeklyXp: List<Pair<String, Long>> = emptyList(), // last 7 weeks label→xp
    val insights: List<AnalysisInsight> = emptyList(),
    val directives: List<ProgramDirective> = emptyList(),
    val recentTrackingLogs: List<MissionTrackingLogEntity> = emptyList(),
    val categoryStats: List<CategoryStats> = emptyList(), // 6 categories, last 30 days
    val weeklyMuscleCoverage: List<MuscleCoverageEntry> = emptyList(),
    val totalMissionsCompleted: Int = 0,
    val totalXpEarned: Long = 0L,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val today: LocalDate = LocalDate.MIN,
    val joinDate: LocalDate = LocalDate.MIN,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val logDao: DailyLogDao,
    private val missionDao: MissionDao,
    private val missionTrackingDao: MissionTrackingDao,
    private val userRepository: UserRepository,
    private val weeklyCoverageUseCase: GetWeeklyMuscleCoverageUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            val sessionDate = timeProvider.sessionDay()
            val start = sessionDate.minusDays(90)
            val fmt = DateTimeFormatter.ISO_LOCAL_DATE

            val logs = logDao.getLogsInRange(start.format(fmt), sessionDate.format(fmt))
            val logMap = logs.associateBy { it.date }

            // Build 91-day calendar
            val days = (90 downTo 0).map { daysAgo ->
                val d = sessionDate.minusDays(daysAgo.toLong())
                val log = logMap[d.format(fmt)]
                DayEntry(
                    date = d,
                    completionRate = log?.completionRate ?: 0f,
                    hasData = log != null,
                    xpGained = log?.xpGained ?: 0,
                    totalMissions = log?.totalMissions ?: 0,
                    wasRestDay = log?.wasRestDay == true
                )
            }

            // Weekly XP (last 7 weeks)
            val weeklyXp = (6 downTo 0).map { weeksAgo ->
                val weekStart = sessionDate.minusWeeks(weeksAgo.toLong()).with(java.time.DayOfWeek.MONDAY)
                val weekEnd = weekStart.plusDays(6)
                val weekLogs = logs.filter {
                    val d = LocalDate.parse(it.date, fmt)
                    !d.isBefore(weekStart) && !d.isAfter(weekEnd)
                }
                val label = if (weeksAgo == 0) "This\nWeek" else "W-$weeksAgo"
                val xp = weekLogs.sumOf { it.xpGained.toLong() }
                label to xp
            }

            // Category pain-point stats (last 30 days of missions)
            val categoryStats = buildCategoryStats(sessionDate, fmt)

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

            // User profile stats
            val profile = userRepository.getUserProfile()
            val joinDate = profile?.joinDate ?: sessionDate
            val recentCompletionRate = logs
                .filter { !LocalDate.parse(it.date, fmt).isBefore(sessionDate.minusDays(6)) }
                .map { it.completionRate }
                .average()
                .toFloat()
                .takeIf { !it.isNaN() }
                ?: 0f

            val directives = buildProgramDirectives(
                profile = profile,
                weeklyCoverage = weeklyCoverage,
                recentCompletionRate = recentCompletionRate
            )
            val recentTrackingLogs = missionTrackingDao.getRecentLogs(limit = 20)

            // Analysis insights
            val insights = buildInsights(days, logs)

            _state.value = HistoryUiState(
                calendarDays = days,
                weeklyXp = weeklyXp,
                insights = insights,
                directives = directives,
                recentTrackingLogs = recentTrackingLogs,
                categoryStats = categoryStats,
                weeklyMuscleCoverage = weeklyCoverage,
                totalMissionsCompleted = profile?.totalMissionsCompleted ?: 0,
                totalXpEarned = profile?.totalXpEarned ?: 0L,
                bestStreak = profile?.streakBest ?: 0,
                currentStreak = profile?.streakCurrent ?: 0,
                today = sessionDate,
                joinDate = joinDate,
                isLoading = false
            )
        }
    }

    private fun buildInsights(days: List<DayEntry>, logs: List<DailyLogEntity>): List<AnalysisInsight> {
        val insights = mutableListOf<AnalysisInsight>()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val sessionDate = timeProvider.sessionDay()

        // 1. This week vs last week completion rate
        val thisWeekDays = days.filter { !it.date.isBefore(sessionDate.minusDays(6)) && it.hasData }
        val lastWeekDays = days.filter {
            val d = it.date
            !d.isBefore(sessionDate.minusDays(13)) && d.isBefore(sessionDate.minusDays(6)) && it.hasData
        }
        val thisWeekAvg = if (thisWeekDays.isEmpty()) 0f else thisWeekDays.map { it.completionRate }.average().toFloat()
        val lastWeekAvg = if (lastWeekDays.isEmpty()) 0f else lastWeekDays.map { it.completionRate }.average().toFloat()
        val weekDelta = thisWeekAvg - lastWeekAvg
        insights.add(AnalysisInsight(
            label = "This Week vs Last Week",
            value = "${(thisWeekAvg * 100).toInt()}% avg completion",
            subtext = if (weekDelta >= 0) "+${(weekDelta * 100).toInt()}% — improving" else "${(weekDelta * 100).toInt()}% — slipping",
            isPositive = weekDelta >= 0
        ))

        // 2. Best day of week
        if (logs.isNotEmpty()) {
            val dayOfWeekRates = (0..6).map { dow ->
                val dayLogs = logs.filter { LocalDate.parse(it.date, fmt).dayOfWeek.value % 7 == dow }
                val avg = if (dayLogs.isEmpty()) 0f else dayLogs.map { it.completionRate }.average().toFloat()
                Pair(dow, avg)
            }
            val best = dayOfWeekRates.maxByOrNull { it.second }
            val worst = dayOfWeekRates.filter { it.second > 0 }.minByOrNull { it.second }
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            if (best != null && best.second > 0) {
                insights.add(AnalysisInsight(
                    label = "Power Day",
                    value = dayNames[best.first],
                    subtext = "${(best.second * 100).toInt()}% avg — your strongest day",
                    isPositive = true
                ))
            }
            if (worst != null && worst.second < best?.second ?: 1f) {
                insights.add(AnalysisInsight(
                    label = "Danger Day",
                    value = dayNames[worst.first],
                    subtext = "${(worst.second * 100).toInt()}% avg — needs attention",
                    isPositive = false
                ))
            }
        }

        // 3. 30-day completion rate
        val last30 = days.filter { it.hasData && !it.date.isBefore(sessionDate.minusDays(29)) }
        if (last30.isNotEmpty()) {
            val avg30 = last30.map { it.completionRate }.average().toFloat()
            val perfectDays = last30.count { it.completionRate >= 1.0f }
            insights.add(AnalysisInsight(
                label = "30-Day Average",
                value = "${(avg30 * 100).toInt()}%",
                subtext = "$perfectDays perfect days in the last month",
                isPositive = avg30 >= 0.6f
            ))
        }

        // 4. Current momentum (last 7 days trend)
        val last7 = days.filter { it.hasData && !it.date.isBefore(sessionDate.minusDays(6)) }
        val prev7 = days.filter { it.hasData && !it.date.isBefore(sessionDate.minusDays(13)) && it.date.isBefore(sessionDate.minusDays(6)) }
        if (last7.isNotEmpty() && prev7.isNotEmpty()) {
            val last7Avg = last7.map { it.completionRate }.average().toFloat()
            val prev7Avg = prev7.map { it.completionRate }.average().toFloat()
            val momentum = last7Avg - prev7Avg
            insights.add(AnalysisInsight(
                label = "Momentum",
                value = if (momentum >= 0) "↑ Rising" else "↓ Declining",
                subtext = if (momentum >= 0) "Completion improving over last 2 weeks" else "Completion declining — recalibrate",
                isPositive = momentum >= 0
            ))
        }

        return insights
    }

    private suspend fun buildCategoryStats(today: LocalDate, fmt: DateTimeFormatter): List<CategoryStats> {
        val from = today.minusDays(30).format(fmt)
        val to = today.format(fmt)
        val missions = missionDao.getMissionsInRange(from, to)

        return MissionCategory.entries.map { category ->
            val catMissions = missions.filter { it.category == category.name }
            CategoryStats(
                category = category,
                total = catMissions.size,
                completed = catMissions.count { it.isCompleted },
                failed = catMissions.count { it.isFailed },
                skipped = catMissions.count { it.isSkipped },
                miniAccepted = catMissions.count { it.acceptedMiniVersion }
            )
        }
    }
}
