package com.desklauncher.feature.calculator.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** 결과 표시: 천 단위 쉼표, 소수 최대 [MaxFraction]자리, 너무 크거나 작으면 지수 표기. */
object NumberFormatter {
    private const val MaxFraction = 10
    private val Large = BigDecimal("1E15")
    private val Small = BigDecimal("1E-10")

    fun format(value: BigDecimal): String {
        val abs = value.abs()
        if (abs.signum() != 0 && (abs >= Large || abs < Small)) {
            return value.round(MathContext(10, RoundingMode.HALF_EVEN)).stripTrailingZeros().toString().replace("E+", "E")
        }
        val rounded = value.setScale(MaxFraction, RoundingMode.HALF_EVEN).stripTrailingZeros()
        val plain = if (rounded.signum() == 0) "0" else rounded.toPlainString()
        val negative = plain.startsWith("-")
        val digits = plain.removePrefix("-")
        val integer = digits.substringBefore('.')
        val fraction = digits.substringAfter('.', "")
        val grouped = integer.reversed().chunked(3).joinToString(",").reversed()
        return (if (negative) "-" else "") + grouped + if (fraction.isNotEmpty()) ".$fraction" else ""
    }
}
