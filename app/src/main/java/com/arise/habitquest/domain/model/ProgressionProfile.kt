package com.arise.habitquest.domain.model

enum class EquipmentMode {
    BODYWEIGHT,
    MIXED,
    GYM
}

enum class ScheduleStyle {
    FIXED_WINDOW,
    FLEXIBLE_SPLIT
}

enum class ProgressionPreference {
    CONSERVATIVE,
    ASSERTIVE_SAFE,
    AGGRESSIVE
}

enum class ProgressionState {
    PROGRESSING,
    HOLD,
    DELOAD,
    RE_RAMP
}
