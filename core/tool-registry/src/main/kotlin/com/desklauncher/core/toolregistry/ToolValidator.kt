package com.desklauncher.core.toolregistry

import com.desklauncher.core.toolapi.DeskToolApi

/**
 * 등록된 도구를 검사해 실을 것과 뺄 것을 나눈다.
 * 런처는 앱이 죽으면 홈 화면이 사라지므로, 잘못 만든 플러그인 하나 때문에 크래시 나지 않고 그 플러그인만 뺀다.
 */
internal object ToolValidator {

    data class Candidate(val id: String, val apiVersion: Int, val source: String)

    sealed interface Rejection {
        val source: String
        data class InvalidId(val id: String, override val source: String) : Rejection
        data class UnsupportedApi(val id: String, val apiVersion: Int, override val source: String) : Rejection
        data class DuplicateId(val id: String, override val source: String) : Rejection
    }

    data class Result(val accepted: List<Int>, val rejected: List<Rejection>)

    /** @return 받아들인 후보의 인덱스(입력 순서 유지)와 거절 사유. 중복 id는 먼저 온 것만 남긴다. */
    fun validate(candidates: List<Candidate>, hostApiVersion: Int = DeskToolApi.VERSION): Result {
        val accepted = mutableListOf<Int>()
        val rejected = mutableListOf<Rejection>()
        val seen = mutableSetOf<String>()
        candidates.forEachIndexed { index, candidate ->
            when {
                !DeskToolApi.IdPattern.matches(candidate.id) ->
                    rejected += Rejection.InvalidId(candidate.id, candidate.source)
                candidate.apiVersion !in 1..hostApiVersion ->
                    rejected += Rejection.UnsupportedApi(candidate.id, candidate.apiVersion, candidate.source)
                !seen.add(candidate.id) ->
                    rejected += Rejection.DuplicateId(candidate.id, candidate.source)
                else -> accepted += index
            }
        }
        return Result(accepted, rejected)
    }
}
