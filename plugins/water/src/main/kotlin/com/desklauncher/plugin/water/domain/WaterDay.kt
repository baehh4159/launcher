package com.desklauncher.plugin.water.domain

import java.time.LocalDate

/** 하루치 물 기록 */
data class WaterDay(
    val date: LocalDate,
    val cups: Int,
    val goal: Int,
) {
    val reachedGoal: Boolean get() = cups >= goal

    companion object {
        const val DefaultGoal: Int = 8

        /** 실수로 연타해도 의미 없는 값까지 가지 않게 상한을 둔다 */
        const val MaxCups: Int = 30

        fun addCups(current: Int, delta: Int): Int = (current + delta).coerceIn(0, MaxCups)
    }
}
