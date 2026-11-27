package com.desklauncher.core.toolregistry

import com.desklauncher.core.toolregistry.ToolValidator.Candidate
import com.desklauncher.core.toolregistry.ToolValidator.Rejection
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolValidatorTest {

    // 올바른 도구는 입력 순서대로 모두 실린다
    @Test
    fun validate_acceptsAll_whenValid() {
        val result = ToolValidator.validate(listOf(c("todo"), c("water_log"), c("a1")), hostApiVersion = 1)

        assertEquals(listOf(0, 1, 2), result.accepted)
        assertEquals(emptyList<Rejection>(), result.rejected)
    }

    // id 형식이 틀리면(대문자·하이픈·한 글자·숫자 시작·빈 값) 뺀다
    @Test
    fun validate_rejectsInvalidId() {
        val ids = listOf("Water", "water-log", "w", "1water", "", "a".repeat(33))
        val result = ToolValidator.validate(ids.map(::c), hostApiVersion = 1)

        assertEquals(emptyList<Int>(), result.accepted)
        assertEquals(ids, result.rejected.map { (it as Rejection.InvalidId).id })
    }

    // 앱보다 새 SDK로 만든 플러그인과 0 이하 버전은 뺀다
    @Test
    fun validate_rejectsUnsupportedApiVersion() {
        val result = ToolValidator.validate(listOf(c("old", 1), c("new", 3), c("zero", 0)), hostApiVersion = 2)

        assertEquals(listOf(0), result.accepted)
        assertEquals(listOf("new", "zero"), result.rejected.map { (it as Rejection.UnsupportedApi).id })
    }

    // 같은 id가 둘이면 먼저 온 것만 남기고 크래시하지 않는다
    @Test
    fun validate_keepsFirst_whenDuplicateId() {
        val result = ToolValidator.validate(listOf(c("memo", source = "A"), c("memo", source = "B")), hostApiVersion = 1)

        assertEquals(listOf(0), result.accepted)
        assertEquals(listOf(Rejection.DuplicateId("memo", "B")), result.rejected)
    }

    private fun c(id: String, apiVersion: Int = 1, source: String = id) = Candidate(id, apiVersion, source)
}
