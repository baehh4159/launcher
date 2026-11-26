package com.desklauncher.feature.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class ExpressionTest {

    private fun value(input: String): String =
        (Expression.evaluate(input) as CalcResult.Value).value.toPlainString()

    // 우선순위·괄호
    @Test
    fun evaluate_respectsPrecedenceAndParentheses() {
        assertEquals("14", value("2+3×4"))
        assertEquals("20", value("(2+3)×4"))
        assertEquals("2", value("8÷2÷2"))
        assertEquals("-6", value("−(2+4)"))
        assertEquals("1", value("3−2"))
        assertEquals("7", value("2*3+1"))
    }

    // BigDecimal: 부동소수 오차 없음
    @Test
    fun evaluate_isExactForDecimals() {
        assertEquals("0.3", value("0.1+0.2"))
        assertEquals("0.3333333333333333333", value("1÷3").take(21))
    }

    // 퍼센트: a ± b% = a의 b%, 곱셈·단독은 /100
    @Test
    fun evaluate_percentSemantics() {
        assertEquals("220", value("200+10%"))
        assertEquals("180", value("200−10%"))
        assertEquals("5", value("50×10%"))
        assertEquals("0.5", value("50%"))
    }

    // 단항 부호와 끝 괄호 생략
    @Test
    fun evaluate_unaryAndUnclosedParenthesis() {
        assertEquals("-3", value("−3"))
        assertEquals("6", value("2×(1+2"))
        assertEquals("-2", value("3×−2+4"))
    }

    @Test
    fun evaluate_reportsErrors() {
        assertEquals(CalcResult.Error(CalcError.DivideByZero), Expression.evaluate("5÷0"))
        assertEquals(CalcResult.Error(CalcError.Syntax), Expression.evaluate("2××3"))
        assertEquals(CalcResult.Error(CalcError.Syntax), Expression.evaluate("1.2.3"))
        assertEquals(CalcResult.Error(CalcError.Syntax), Expression.evaluate(""))
        assertEquals(CalcResult.Error(CalcError.Syntax), Expression.evaluate("2)"))
    }

    // 미리보기: 끝 연산자 무시, 숫자만이면 없음
    @Test
    fun preview_ignoresTrailingOperators() {
        assertEquals(BigDecimal("5"), Expression.preview("2+3+"))
        assertEquals(BigDecimal("6"), Expression.preview("2×(3"))
        assertNull(Expression.preview("42"))
        assertNull(Expression.preview("5÷0"))
    }

    @Test
    fun format_groupsAndTrims() {
        assertEquals("1,234,567.89", NumberFormatter.format(BigDecimal("1234567.890")))
        assertEquals("-1,000", NumberFormatter.format(BigDecimal("-1000")))
        assertEquals("0", NumberFormatter.format(BigDecimal("0.000")))
        assertEquals("0.3333333333", NumberFormatter.format(BigDecimal("0.33333333333333")))
        assertEquals("1.234567891E17", NumberFormatter.format(BigDecimal("123456789123456789")))
    }
}
