package com.arise.habitquest.domain.model

fun progressPercent(current: Int, target: Int): Float =
    if (target > 0) current.toFloat() / target else 0f

fun progressPercent(current: Long, target: Long): Float =
    if (target > 0) current.toFloat() / target else 0f
