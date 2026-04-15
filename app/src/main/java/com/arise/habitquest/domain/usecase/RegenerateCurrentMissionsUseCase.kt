package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.RestDayPolicy
import javax.inject.Inject

class RegenerateCurrentMissionsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val generateDailyMissions: GenerateDailyMissionsUseCase,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke() {
        val profile = userRepository.getUserProfile() ?: return
        val sessionDate = timeProvider.sessionDay()
        val isRestDay = RestDayPolicy.isRestDay(sessionDate, profile.restDay)

        missionRepository.deleteDailyMissionsForDate(sessionDate)

        if (!isRestDay) {
            generateDailyMissions(profile, sessionDate)
        }
    }
}