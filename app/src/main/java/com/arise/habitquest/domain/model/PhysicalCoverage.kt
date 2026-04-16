package com.arise.habitquest.domain.model

/**
 * Coarse movement families used for weekly physical workload balancing.
 */
enum class PhysicalMissionFamily {
    UNSPECIFIED,
    PUSH,
    PULL,
    LOWER_BODY,
    CARDIO_NEAT,
    CORE_STABILITY,
    MOBILITY_PREHAB,
    RECOVERY_SUPPORT,
    FULL_BODY
}

/**
 * Body-region coverage buckets for workload tracking and future body chart UI.
 */
enum class MuscleRegion {
    CHEST,
    SHOULDERS,
    TRICEPS,
    UPPER_BACK_LATS,
    BICEPS,
    CORE,
    GLUTES,
    QUADS,
    HAMSTRINGS,
    CALVES,
    CARDIO_CONDITIONING
}
