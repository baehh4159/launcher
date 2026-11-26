package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 앱 서랍 개인화 (PRD §8.3 app_pref). */
@Entity(tableName = "app_pref")
data class AppPrefEntity(
    /** `패키지/액티비티` (ComponentName.flattenToString) */
    @PrimaryKey
    @ColumnInfo(name = "componentKey")
    val componentKey: String,
    @ColumnInfo(name = "hidden", defaultValue = "0")
    val hidden: Boolean = false,
    @ColumnInfo(name = "favorite", defaultValue = "0")
    val favorite: Boolean = false,
    @ColumnInfo(name = "launchCount", defaultValue = "0")
    val launchCount: Int = 0,
    @ColumnInfo(name = "lastLaunchedAt")
    val lastLaunchedAt: Long? = null,
)
