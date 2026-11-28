package com.desklauncher.feature.apps.domain

/**
 * 고정 앱 줄 규칙 (APPS-02, PRD §12) — 순수 함수.
 * 최대 [Max]개, 사용자 순서. 결과는 새 목록 또는 거절 사유.
 */
object PinnedApps {
    const val Max = 5

    sealed interface Result {
        data class Updated(val keys: List<String>) : Result
        /** 이미 5개 — 고정할 수 없다고 알린다 */
        data object Full : Result
        /** 변화 없음 (이미 고정됨, 없는 앱, 끝에서 더 못 감) */
        data object Unchanged : Result
    }

    fun pin(keys: List<String>, key: String): Result = when {
        key in keys -> Result.Unchanged
        keys.size >= Max -> Result.Full
        else -> Result.Updated(keys + key)
    }

    fun unpin(keys: List<String>, key: String): Result =
        if (key in keys) Result.Updated(keys - key) else Result.Unchanged

    /** [delta] -1 = 앞으로(왼쪽), +1 = 뒤로(오른쪽) */
    fun move(keys: List<String>, key: String, delta: Int): Result {
        val from = keys.indexOf(key)
        if (from < 0) return Result.Unchanged
        val to = from + delta
        if (to !in keys.indices || to == from) return Result.Unchanged
        return Result.Updated(keys.toMutableList().apply { add(to, removeAt(from)) })
    }

    /** 저장된 순서대로, 지금 설치된 앱만 (삭제된 앱은 줄에서 빠진다) */
    fun <T> resolve(keys: List<String>, installed: Map<String, T>): List<T> = keys.mapNotNull(installed::get)
}
