package com.arise.habitquest.domain.model

/**
 * Life themes the hunter can set to bias their daily mission generation.
 * Up to 3 can be active simultaneously.
 */
enum class FocusTheme(
    val displayName: String,
    val subtitle: String,
    val primaryCategory: MissionCategory,
    val secondaryCategory: MissionCategory?,
    val iconName: String
) {
    PHYSICAL_PERFORMANCE(
        "Physical Performance",
        "Build strength, endurance & athleticism",
        MissionCategory.PHYSICAL,
        MissionCategory.WELLNESS,
        "fitness_center"
    ),
    MENTAL_CLARITY(
        "Mental Clarity",
        "Sharpen focus, learning & discipline",
        MissionCategory.MENTAL,
        MissionCategory.WELLNESS,
        "psychology"
    ),
    CAREER_PRODUCTIVITY(
        "Career & Productivity",
        "Output, growth & professional edge",
        MissionCategory.PRODUCTIVITY,
        MissionCategory.MENTAL,
        "work"
    ),
    SOCIAL_CONNECTION(
        "Social Connection",
        "Relationships, communication & community",
        MissionCategory.SOCIAL,
        null,
        "group"
    ),
    CREATIVE_EXPRESSION(
        "Creative Expression",
        "Create, build & express without limits",
        MissionCategory.CREATIVITY,
        MissionCategory.MENTAL,
        "palette"
    ),
    WELLNESS_RECOVERY(
        "Wellness & Recovery",
        "Sleep, stress, nutrition & inner calm",
        MissionCategory.WELLNESS,
        MissionCategory.PHYSICAL,
        "spa"
    )
}
