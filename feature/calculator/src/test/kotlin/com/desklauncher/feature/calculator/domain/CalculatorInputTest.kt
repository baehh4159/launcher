package com.desklauncher.feature.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorInputTest {

    private fun type(vararg keys: CalcKey) = keys.fold(CalculatorInput()) { input, key -> input.press(key) }
    private fun d(n: Int) = CalcKey.Digit(n)
    private fun op(c: Char) = CalcKey.Operator(c)

    // 연산자를 연달아 누르면 마지막 것으로 바뀐다 (×− 음수 입력은 허용)
    @Test
    fun operator_replacesPreviousOperator() {
        assertEquals("2×", type(d(2), op('+'), op('×')).expression)
        assertEquals("2×−", type(d(2), op('×'), op('−')).expression)
        assertEquals("2+", type(d(2), op('×'), op('−'), op('+')).expression)
    }

    // '=' 뒤 숫자는 새로, 연산자는 결과에 이어서
    @Test
    fun equals_thenDigitStartsNew_thenOperatorContinues() {
        val evaluated = type(d(2), op('+'), d(3), CalcKey.Equals)
        assertEquals("5", evaluated.expression)
        assertTrue(evaluated.justEvaluated)
        assertEquals("7", evaluated.press(d(7)).expression)
        assertEquals("5×", evaluated.press(op('×')).expression)
    }

    // 소수점은 숫자당 한 번, 빈 곳에서는 0.
    @Test
    fun dot_onlyOncePerNumber() {
        assertEquals("0.5", type(CalcKey.Dot, d(5)).expression)
        assertEquals("1.2", type(d(1), CalcKey.Dot, d(2), CalcKey.Dot).expression)
    }

    // 괄호 키: 열 수 있으면 열고, 닫을 수 있으면 닫는다. 숫자 뒤 여는 괄호는 곱셈
    @Test
    fun parentheses_areSmart() {
        assertEquals("(2+3)", type(CalcKey.Parentheses, d(2), op('+'), d(3), CalcKey.Parentheses).expression)
        assertEquals("2×(", type(d(2), CalcKey.Parentheses).expression)
    }

    // 부호 바꾸기는 마지막 숫자에만
    @Test
    fun toggleSign_wrapsLastNumber() {
        assertEquals("5×(−3", type(d(5), op('×'), d(3), CalcKey.ToggleSign).expression)
        assertEquals("5×3", type(d(5), op('×'), d(3), CalcKey.ToggleSign, CalcKey.ToggleSign).expression)
    }

    // 오류는 표시하고 다음 키에서 지운다
    @Test
    fun error_isShownThenClearedOnNextKey() {
        val error = type(d(5), op('÷'), d(0), CalcKey.Equals)
        assertEquals(CalcError.DivideByZero, error.error)
        assertEquals(null, error.press(CalcKey.Backspace).error)
    }

    // 음수 결과도 화면 기호로
    @Test
    fun negativeResult_usesDisplayMinus() {
        assertEquals("−1", type(d(2), op('−'), d(3), CalcKey.Equals).expression)
    }
}
