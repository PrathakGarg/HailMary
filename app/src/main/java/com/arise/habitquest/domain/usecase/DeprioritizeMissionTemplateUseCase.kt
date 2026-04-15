package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.generator.MissionTemplates
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionExclusions
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

class DeprioritizeMissionTemplateUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val dataStore: OnboardingDataStore,
    private val generator: MissionGenerator,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(missionId: String): Boolean {
        val sessionDate = timeProvider.sessionDay()
        val mission = missionRepository.getMissionById(missionId) ?: return false
        val templateId = mission.parentTemplateId ?: return false

        if (mission.type != MissionType.DAILY || !mission.isActive || mission.dueDate != sessionDate) {
            return false
        }

        val profile = userRepository.getUserProfile() ?: return false

        val existingDeprioritized = dataStore.deprioritizedTemplateIds.first()
        val updatedDeprioritized = existingDeprioritized + templateId
        dataStore.setDeprioritizedTemplateIds(updatedDeprioritized)

        val excludedTemplateIds = updatedDeprioritized +
            if (dataStore.excludeInboxMissions.first()) MissionExclusions.INBOX_TEMPLATE_IDS else emptySet()

        val todayMissions = missionRepository.getMissionsForDate(sessionDate)
        val todayTemplateIds = todayMissions.mapNotNull { it.parentTemplateId }.toSet() + templateId

        val sameCategoryCandidates = MissionTemplates.all.filter {
            it.category == mission.category &&
                it.id !in excludedTemplateIds &&
                it.id !in todayTemplateIds
        }
        val fallbackCandidates = MissionTemplates.all.filter {
            it.id !in excludedTemplateIds &&
                it.id !in todayTemplateIds
        }

        val candidatePool = if (sameCategoryCandidates.isNotEmpty()) sameCategoryCandidates else fallbackCandidates
        val replacementTemplate = candidatePool
            .ifEmpty { return false }
            .shuffled(Random(sessionDate.toEpochDay() + missionId.hashCode().toLong()))
            .first()

        val completionCount = userRepository.getShadowCompletions(listOf(replacementTemplate.id))[replacementTemplate.id] ?: 0
        val replacement = generator.generateSingleDailyMission(
            profile = profile,
            templateId = replacementTemplate.id,
            date = sessionDate,
            shadowCompletions = completionCount
        ) ?: return false

        missionRepository.deleteMissionById(mission.id)
        missionRepository.insertMission(replacement)
        return true
    }
}
