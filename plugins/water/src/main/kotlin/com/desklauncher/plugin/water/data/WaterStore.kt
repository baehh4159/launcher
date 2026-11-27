package com.desklauncher.plugin.water.data

import com.desklauncher.core.toolapi.PluginStorage
import com.desklauncher.plugin.water.domain.WaterDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * 플러그인 전용 저장소([PluginStorage]) 위의 얇은 저장 계층.
 * 키 규칙: "day:YYYY-MM-DD" → 잔 수, "goal" → 목표. 자기 표·마이그레이션이 필요 없다.
 */
class WaterStore(private val storage: PluginStorage) {

    fun observeDay(date: LocalDate): Flow<WaterDay> =
        combine(storage.observe(dayKey(date)), storage.observe(GoalKey)) { cups, goal ->
            WaterDay(
                date = date,
                cups = cups?.toIntOrNull() ?: 0,
                goal = goal?.toIntOrNull() ?: WaterDay.DefaultGoal,
            )
        }

    suspend fun addCups(date: LocalDate, delta: Int) {
        val key = dayKey(date)
        val current = storage.get(key)?.toIntOrNull() ?: 0
        storage.put(key, WaterDay.addCups(current, delta).toString())
    }

    private fun dayKey(date: LocalDate): String = "day:$date"

    private companion object {
        const val GoalKey = "goal"
    }
}
