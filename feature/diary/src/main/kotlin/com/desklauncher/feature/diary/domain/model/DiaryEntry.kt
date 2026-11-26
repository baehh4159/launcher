package com.desklauncher.feature.diary.domain.model

import java.time.Instant
import java.time.LocalDate

data class DiaryEntry(
    val id: String,
    val date: LocalDate,
    val mood: Mood? = null,
    val body: String = "",
    val createdAt: Instant,
    val updatedAt: Instant,
)

/** 기분 5단계 (DIARY-01). 저장값은 DB 이름 */
enum class Mood(val dbName: String) {
    VeryBad("VERY_BAD"),
    Bad("BAD"),
    Okay("OKAY"),
    Good("GOOD"),
    VeryGood("VERY_GOOD"),
    ;

    companion object {
        fun fromDb(value: String?): Mood? = entries.firstOrNull { it.dbName == value }
    }
}
