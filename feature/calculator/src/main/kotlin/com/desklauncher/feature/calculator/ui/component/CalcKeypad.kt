package com.desklauncher.feature.calculator.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.toolTabColor
import com.desklauncher.feature.calculator.R
import com.desklauncher.feature.calculator.domain.CalcKey
import com.desklauncher.feature.calculator.CalculatorToolId

private data class KeySpec(val label: String, val key: CalcKey, val kind: KeyKind, val description: Int? = null)

private enum class KeyKind { Number, Function, Operator, Equals }

/** 5×4 키패드. 엄지 영역(화면 아래)에 둔다. */
@Composable
internal fun CalcKeypad(
    onKey: (CalcKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberDeskHaptics()
    val rows = listOf(
        listOf(
            KeySpec("C", CalcKey.Clear, KeyKind.Function, R.string.calculator_key_clear),
            KeySpec("( )", CalcKey.Parentheses, KeyKind.Function, R.string.calculator_key_parentheses),
            KeySpec("%", CalcKey.Percent, KeyKind.Function, R.string.calculator_key_percent),
            KeySpec("÷", CalcKey.Operator('÷'), KeyKind.Operator, R.string.calculator_key_divide),
        ),
        listOf(digit(7), digit(8), digit(9), KeySpec("×", CalcKey.Operator('×'), KeyKind.Operator, R.string.calculator_key_multiply)),
        listOf(digit(4), digit(5), digit(6), KeySpec("−", CalcKey.Operator('−'), KeyKind.Operator, R.string.calculator_key_minus)),
        listOf(digit(1), digit(2), digit(3), KeySpec("+", CalcKey.Operator('+'), KeyKind.Operator, R.string.calculator_key_plus)),
        listOf(
            KeySpec("±", CalcKey.ToggleSign, KeyKind.Number, R.string.calculator_key_toggle_sign),
            digit(0),
            KeySpec(".", CalcKey.Dot, KeyKind.Number, R.string.calculator_key_dot),
            KeySpec("=", CalcKey.Equals, KeyKind.Equals, R.string.calculator_key_equals),
        ),
    )
    Column(modifier = modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { spec ->
                    CalcButton(spec, onClick = {
                        haptics.keyTap()
                        onKey(spec.key)
                    }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun digit(n: Int) = KeySpec(n.toString(), CalcKey.Digit(n), KeyKind.Number)

@Composable
private fun CalcButton(spec: KeySpec, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (spec.kind) {
        KeyKind.Number -> colors.surfaceVariant to colors.onSurface
        KeyKind.Function -> colors.secondaryContainer to colors.onSecondaryContainer
        KeyKind.Operator -> toolTabColor(CalculatorToolId, DeskTabColor.Teal).container.copy(alpha = 0.22f) to toolTabColor(CalculatorToolId, DeskTabColor.Teal).container
        KeyKind.Equals -> toolTabColor(CalculatorToolId, DeskTabColor.Teal).container to toolTabColor(CalculatorToolId, DeskTabColor.Teal).onContainer
    }
    val description = spec.description?.let { stringResource(it) }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
        contentColor = content,
        modifier = modifier
            .aspectRatio(1.25f)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(spec.label, style = MaterialTheme.typography.headlineSmall, color = content)
        }
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun CalcKeypadPreview() {
    DeskTheme { CalcKeypad(onKey = {}) }
}
