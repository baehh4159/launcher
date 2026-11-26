package com.desklauncher.feature.diary.domain.repository

import com.desklauncher.feature.diary.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DiaryRepository {
    /** 최신 날짜 순 */
    val entries: Flow<List<DiaryEntry>>
    suspend fun get(date: LocalDate): DiaryEntry?

    /** 날짜당 1건 — 같은 날짜면 덮어쓴다 */
    suspend fun save(entry: DiaryEntry)
    suspend fun delete(date: LocalDate)
}
