package com.desklauncher.feature.alarm.domain.repository

import com.desklauncher.feature.alarm.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    /** 시각 순 */
    val alarms: Flow<List<Alarm>>
    suspend fun get(id: String): Alarm?
    suspend fun getEnabled(): List<Alarm>
    suspend fun save(alarm: Alarm)
    suspend fun delete(id: String)
}
