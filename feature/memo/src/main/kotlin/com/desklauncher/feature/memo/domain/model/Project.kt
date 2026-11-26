package com.desklauncher.feature.memo.domain.model

import java.time.Instant

data class Project(
    val id: String,
    val name: String,
    /** DeskTabColor 이름 */
    val colorName: String,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
