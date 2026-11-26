package com.desklauncher.feature.calculator.domain

import java.io.Serializable

/** 계산기 키 (CALC-01) */
sealed interface CalcKey {
    data class Digit(val value: Int) : CalcKey
    data class Operator(val symbol: Char) : CalcKey
    data object Dot : CalcKey
    data object Parentheses : CalcKey
    data object Percent : CalcKey
    data object ToggleSign : CalcKey
    data object Backspace : CalcKey
    data object Clear : CalcKey
    data object Equals : CalcKey
}

/**
 * 계산기 입력 상태와 키 처리 (순수). 화면 기호(+ − × ÷)로 수식을 만든다.
 * [justEvaluated]: '=' 직후 — 숫자를 누르면 새로 시작, 연산자를 누르면 결과에 이어서 계산.
 */
data class CalculatorInput(
    val expression: String = "",
    val justEvaluated: Boolean = false,
    val error: CalcError? = null,
) : Serializable {

    fun press(key: CalcKey): CalculatorInput {
        val base = if (error != null) copy(error = null) else this
        return when (key) {
            is CalcKey.Digit -> base.typeDigit(key.value)
            is CalcKey.Operator -> base.typeOperator(key.symbol)
            CalcKey.Dot -> base.typeDot()
            CalcKey.Parentheses -> base.typeParenthesis()
            CalcKey.Percent -> base.typePercent()
            CalcKey.ToggleSign -> base.toggleSign()
            CalcKey.Backspace -> base.copy(expression = base.expression.dropLast(1), justEvaluated = false)
            CalcKey.Clear -> CalculatorInput()
            CalcKey.Equals -> base.evaluate()
        }
    }

    private val last: Char? get() = expression.lastOrNull()

    private fun typeDigit(digit: Int): CalculatorInput {
        val start = if (justEvaluated) "" else expression
        // ')' 나 '%' 뒤 숫자는 곱셈으로 이어 준다
        val joiner = if (start.lastOrNull() == ')' || start.lastOrNull() == '%') "×" else ""
        return copy(expression = start + joiner + digit, justEvaluated = false)
    }

    private fun typeOperator(symbol: Char): CalculatorInput {
        val current = expression
        return when {
            current.isEmpty() -> if (symbol == '−') copy(expression = "−") else this
            last in BinaryOps -> {
                // 연산자 바꾸기. 단, '×−'·'÷−'처럼 음수 입력은 허용
                if (symbol == '−' && (last == '×' || last == '÷')) copy(expression = current + symbol, justEvaluated = false)
                else if (current.length == 1) this
                else copy(expression = current.dropLast(1).trimEnd { it in BinaryOps } + symbol, justEvaluated = false)
            }
            last == '(' -> if (symbol == '−') copy(expression = current + symbol) else this
            else -> copy(expression = current + symbol, justEvaluated = false)
        }
    }

    private fun typeDot(): CalculatorInput {
        if (justEvaluated) return copy(expression = "0.", justEvaluated = false)
        val number = expression.takeLastWhile { it.isDigit() || it == '.' }
        if ('.' in number) return this
        val prefix = when {
            number.isNotEmpty() -> ""
            last == ')' || last == '%' -> "×0"
            else -> "0"
        }
        return copy(expression = "$expression$prefix.")
    }

    private fun typeParenthesis(): CalculatorInput {
        val current = if (justEvaluated) expression else expression
        val open = current.count { it == '(' } - current.count { it == ')' }
        val closable = open > 0 && (last?.isDigit() == true || last == ')' || last == '%')
        val addition = when {
            closable -> ")"
            last?.isDigit() == true || last == ')' || last == '%' -> "×("
            else -> "("
        }
        return copy(expression = current + addition, justEvaluated = false)
    }

    private fun typePercent(): CalculatorInput =
        if (last?.isDigit() == true || last == ')') copy(expression = "$expression%", justEvaluated = false) else this

    /** 마지막 숫자의 부호를 바꾼다: 3 → (−3, (−3 → 3 */
    private fun toggleSign(): CalculatorInput {
        val number = expression.takeLastWhile { it.isDigit() || it == '.' }
        if (number.isEmpty()) return if (expression.isEmpty()) copy(expression = "(−") else this
        val head = expression.dropLast(number.length)
        return if (head.endsWith("(−")) {
            copy(expression = head.dropLast(2) + number, justEvaluated = false)
        } else {
            copy(expression = "$head(−$number", justEvaluated = false)
        }
    }

    private fun evaluate(): CalculatorInput {
        if (expression.isEmpty()) return this
        return when (val result = Expression.evaluate(expression)) {
            is CalcResult.Value -> CalculatorInput(expression = result.value.toPlainString().replace('-', '−'), justEvaluated = true)
            is CalcResult.Error -> copy(error = result.reason)
        }
    }

    private companion object {
        val BinaryOps = setOf('+', '−', '×', '÷')
    }
}
