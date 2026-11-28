package com.desklauncher.feature.apps.domain

import com.desklauncher.feature.apps.domain.model.AppSection
import com.desklauncher.feature.apps.domain.model.InstalledApp
import java.text.Collator
import java.util.Locale

/**
 * 앱 서랍 인덱스(초성/알파벳/#)와 검색 — 순수 Kotlin.
 * 한글 음절 = 0xAC00 + (초성 × 21 + 중성) × 28 + 종성.
 */
object AppIndex {

    /** 표시 순서: 한글 초성(쌍자음은 기본 자음으로) → A–Z → # */
    val KeyOrder: List<String> =
        listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ", "ㅁ", "ㅂ", "ㅅ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ") +
            ('A'..'Z').map(Char::toString) +
            listOf(OtherKey)

    const val OtherKey = "#"

    private val Choseong = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
    )
    private val DoubleToSingle = mapOf('ㄲ' to 'ㄱ', 'ㄸ' to 'ㄷ', 'ㅃ' to 'ㅂ', 'ㅆ' to 'ㅅ', 'ㅉ' to 'ㅈ')
    private const val SyllableStart = 0xAC00
    private const val SyllableEnd = 0xD7A3
    private const val SyllablesPerChoseong = 21 * 28

    fun keyOf(label: String): String {
        val first = label.trimStart().firstOrNull() ?: return OtherKey
        val choseong = choseongOf(first)
        return when {
            choseong != null -> (DoubleToSingle[choseong] ?: choseong).toString()
            first.uppercaseChar() in 'A'..'Z' -> first.uppercaseChar().toString()
            else -> OtherKey
        }
    }

    /** "카카오톡" → "ㅋㅋㅇㅌ". 한글이 아닌 글자는 소문자로 둔다. */
    fun choseongString(text: String): String = buildString(text.length) {
        text.forEach { c -> append(choseongOf(c) ?: c.lowercaseChar()) }
    }

    /** 자음만 입력하면 초성 검색, 아니면 이름 포함(대소문자·공백 무시) */
    fun matches(label: String, query: String): Boolean {
        val q = query.filterNot(Char::isWhitespace)
        if (q.isEmpty()) return true
        val compact = label.filterNot(Char::isWhitespace)
        return if (q.all { it in Choseong }) {
            choseongString(compact).contains(q)
        } else {
            compact.contains(q, ignoreCase = true)
        }
    }

    /** 인덱스 키 순서로 묶고, 같은 키 안에서는 한국어 정렬 */
    fun sections(apps: List<InstalledApp>, locale: Locale = Locale.KOREAN): List<AppSection> {
        val collator = Collator.getInstance(locale)
        val rank = KeyOrder.withIndex().associate { it.value to it.index }
        return apps
            .groupBy { keyOf(it.label) }
            .toSortedMap(compareBy { rank[it] ?: Int.MAX_VALUE })
            .map { (key, group) -> AppSection(key, group.sortedWith { a, b -> collator.compare(a.label, b.label) }) }
    }

    private fun choseongOf(c: Char): Char? = when {
        c.code in SyllableStart..SyllableEnd -> Choseong[(c.code - SyllableStart) / SyllablesPerChoseong]
        c in Choseong -> c
        else -> null
    }
}
