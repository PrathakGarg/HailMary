package com.arise.habitquest.domain.usecase.policy

import java.time.DayOfWeek
import java.time.LocalDate

object RestDayPolicy {

    fun isRestDay(date: LocalDate, restDayOrdinal: Int): Boolean =
        date.dayOfWeek == DayOfWeek.of(((restDayOrdinal % 7) + 1).coerceIn(1, 7))
}
