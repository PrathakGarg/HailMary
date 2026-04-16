package com.arise.habitquest.domain.usecase

import com.arise.habitquest.domain.model.MuscleRegion
import com.arise.habitquest.domain.repository.MissionRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class MuscleRegionCoverage(
    val region: MuscleRegion,
    val assignedLoad: Float,
    val completedLoad: Float
)

data class WeeklyMuscleCoverage(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val regions: List<MuscleRegionCoverage>
)

class GetWeeklyMuscleCoverageUseCase @Inject constructor(
    private val missionRepository: MissionRepository
) {
    suspend operator fun invoke(anchorDate: LocalDate): WeeklyMuscleCoverage {
        val weekStart = anchorDate.with(DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(6)
        val missions = missionRepository.getMissionsInRange(weekStart.toString(), weekEnd.toString())
            .filter { it.type.name == "DAILY" }
            .filter { it.category.name == "PHYSICAL" }
            .filter { it.muscleLoad.isNotEmpty() }

        val assigned = mutableMapOf<MuscleRegion, Float>()
        val completed = mutableMapOf<MuscleRegion, Float>()

        missions.forEach { mission ->
            val completionFactor = when {
                mission.isCompleted && mission.acceptedMiniVersion -> 0.6f
                mission.isCompleted -> 1.0f
                else -> 0f
            }

            mission.muscleLoad.forEach { (region, load) ->
                assigned[region] = (assigned[region] ?: 0f) + load
                completed[region] = (completed[region] ?: 0f) + (load * completionFactor)
            }
        }

        val regions = MuscleRegion.entries.map { region ->
            MuscleRegionCoverage(
                region = region,
                assignedLoad = (assigned[region] ?: 0f),
                completedLoad = (completed[region] ?: 0f)
            )
        }

        return WeeklyMuscleCoverage(
            weekStart = weekStart,
            weekEnd = weekEnd,
            regions = regions
        )
    }
}
