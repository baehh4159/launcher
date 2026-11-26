package com.desklauncher.feature.doodle.domain.repository

import com.desklauncher.feature.doodle.domain.model.Doodle
import com.desklauncher.feature.doodle.domain.model.DoodleFile
import kotlinx.coroutines.flow.Flow

interface DoodleRepository {
    /** 최근 수정 순 */
    val doodles: Flow<List<Doodle>>

    suspend fun load(id: String): DoodleFile?

    /** id가 null이면 새로 만든다. 획 JSON + PNG 이미지를 쓰고 id를 돌려준다. */
    suspend fun save(id: String?, file: DoodleFile): String

    /** DB 행과 파일을 함께 지운다 */
    suspend fun delete(id: String)
}
