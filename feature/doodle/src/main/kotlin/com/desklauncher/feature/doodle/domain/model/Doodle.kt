package com.desklauncher.feature.doodle.domain.model

import com.desklauncher.feature.doodle.domain.Stroke
import kotlinx.serialization.Serializable
import java.time.Instant

data class Doodle(
    val id: String,
    val title: String,
    /** 내부 저장소 상대 경로 */
    val imagePath: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/** 획 데이터 파일(doodles/{id}.json) 내용 */
@Serializable
data class DoodleFile(
    val version: Int = 1,
    val width: Int,
    val height: Int,
    val strokes: List<Stroke>,
)
