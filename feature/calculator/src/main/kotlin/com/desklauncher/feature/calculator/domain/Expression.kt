package com.desklauncher.feature.calculator.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * 계산기 수식 파서·계산기 (CALC-02). 외부 라이브러리 없이 재귀 하강으로 구현하고 BigDecimal로 계산한다
 * — 0.1 + 0.2 = 0.3이 정확히 나오도록.
 *
 * 문법
 * ```
 * expr    := term (('+' | '−') term)*
 * term    := unary (('×' | '÷') unary)*
 * unary   := ('−' | '+') unary | postfix
 * postfix := primary '%'*
 * primary := number | '(' expr ')'?      ← 끝의 닫는 괄호는 생략 가능(입력 중 미리보기)
 * ```
 * 퍼센트: `a + b%` / `a − b%`는 a의 b% (200 + 10% = 220), 그 밖에는 b/100 (50 × 10% = 5).
 * 기호는 화면 기호(× ÷ −)와 ASCII(* / -)를 모두 받는다.
 */
object Expression {

    val Context: MathContext = MathContext(20, RoundingMode.HALF_EVEN)

    fun evaluate(input: String): CalcResult = try {
        val parser = Parser(tokenize(input))
        val node = parser.parseExpression()
        parser.expectEnd()
        CalcResult.Value(node.eval().stripTrailingZeros())
    } catch (e: CalcException) {
        CalcResult.Error(e.reason)
    }

    /** 입력 중 미리보기: 끝의 연산자·여는 괄호는 무시하고 계산. 숫자 하나뿐이거나 계산 불가면 null. */
    fun preview(input: String): BigDecimal? {
        var trimmed = input.trimEnd()
        while (trimmed.isNotEmpty() && trimmed.last() in TrailingIgnorable) trimmed = trimmed.dropLast(1).trimEnd()
        if (trimmed.isEmpty() || trimmed.none { it in Operators }) return null
        return (evaluate(trimmed) as? CalcResult.Value)?.value
    }

    // ── 토큰 ─────────────────────────────────────────────
    private sealed interface Token {
        data class Num(val value: BigDecimal) : Token
        data class Op(val symbol: Char) : Token
        data object Open : Token
        data object Close : Token
        data object Percent : Token
    }

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() || c == ',' -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < input.length && (input[i].isDigit() || input[i] == '.' || input[i] == ',')) i++
                    val text = input.substring(start, i).replace(",", "")
                    if (text.count { it == '.' } > 1 || text == ".") throw CalcException(CalcError.Syntax)
                    tokens += Token.Num(BigDecimal(if (text.endsWith(".")) text.dropLast(1) else text))
                }
                c == '(' -> { tokens += Token.Open; i++ }
                c == ')' -> { tokens += Token.Close; i++ }
                c == '%' -> { tokens += Token.Percent; i++ }
                else -> {
                    val op = when (c) {
                        '+' -> '+'
                        '-', '−' -> '−'
                        '*', '×', 'x' -> '×'
                        '/', '÷' -> '÷'
                        else -> throw CalcException(CalcError.Syntax)
                    }
                    tokens += Token.Op(op)
                    i++
                }
            }
        }
        return tokens
    }

    // ── 구문 트리 ─────────────────────────────────────────
    private sealed interface Node {
        fun eval(): BigDecimal
    }

    private data class Number(val value: BigDecimal) : Node {
        override fun eval() = value
    }

    private data class Negate(val operand: Node) : Node {
        override fun eval(): BigDecimal = operand.eval().negate()
    }

    private data class Percent(val operand: Node) : Node {
        override fun eval(): BigDecimal = operand.eval().divide(Hundred, Context)
    }

    private data class Binary(val op: Char, val left: Node, val right: Node) : Node {
        override fun eval(): BigDecimal {
            val l = left.eval()
            // a ± b% → a ± (a × b / 100)
            val r = if ((op == '+' || op == '−') && right is Percent) l.multiply(right.eval(), Context) else right.eval()
            return when (op) {
                '+' -> l.add(r, Context)
                '−' -> l.subtract(r, Context)
                '×' -> l.multiply(r, Context)
                '÷' -> {
                    if (r.signum() == 0) throw CalcException(CalcError.DivideByZero)
                    l.divide(r, Context)
                }
                else -> throw CalcException(CalcError.Syntax)
            }
        }
    }

    private class Parser(private val tokens: List<Token>) {
        private var pos = 0
        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun next(): Token? = tokens.getOrNull(pos++)

        fun expectEnd() {
            if (pos != tokens.size) throw CalcException(CalcError.Syntax)
        }

        fun parseExpression(): Node {
            var node = parseTerm()
            while (true) {
                val op = (peek() as? Token.Op)?.symbol?.takeIf { it == '+' || it == '−' } ?: return node
                next()
                node = Binary(op, node, parseTerm())
            }
        }

        private fun parseTerm(): Node {
            var node = parseUnary()
            while (true) {
                val op = (peek() as? Token.Op)?.symbol?.takeIf { it == '×' || it == '÷' } ?: return node
                next()
                node = Binary(op, node, parseUnary())
            }
        }

        private fun parseUnary(): Node {
            val token = peek()
            if (token is Token.Op && (token.symbol == '−' || token.symbol == '+')) {
                next()
                val operand = parseUnary()
                return if (token.symbol == '−') Negate(operand) else operand
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Node {
            var node = parsePrimary()
            while (peek() == Token.Percent) {
                next()
                node = Percent(node)
            }
            return node
        }

        private fun parsePrimary(): Node = when (val token = next()) {
            is Token.Num -> Number(token.value)
            Token.Open -> {
                val inner = parseExpression()
                if (peek() == Token.Close) next() // 끝의 ')'는 생략 가능
                else if (peek() != null) throw CalcException(CalcError.Syntax)
                inner
            }
            else -> throw CalcException(CalcError.Syntax)
        }
    }

    private class CalcException(val reason: CalcError) : Exception()

    private val Hundred = BigDecimal(100)
    private val Operators = setOf('+', '−', '-', '×', '*', '÷', '/', '%')
    private val TrailingIgnorable = setOf('+', '−', '-', '×', '*', '÷', '/', '(')
}

sealed interface CalcResult {
    data class Value(val value: BigDecimal) : CalcResult
    data class Error(val reason: CalcError) : CalcResult
}

enum class CalcError { Syntax, DivideByZero }
