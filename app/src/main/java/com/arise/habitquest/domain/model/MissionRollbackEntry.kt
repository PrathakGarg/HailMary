package com.arise.habitquest.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MissionRollbackEntry(
    val recordedAtMillis: Long,
    val xpDelta: Long,
    val hpDelta: Int,
    val strDelta: Int,
    val agiDelta: Int,
    val intDelta: Int,
    val vitDelta: Int,
    val endDelta: Int,
    val senseDelta: Int,
    val missionCountDelta: Int,
    val totalXpEarnedDelta: Long
)
