package com.desklauncher.feature.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.calculator.R
import com.desklauncher.feature.calculator.domain.CalcError
import com.desklauncher.feature.calculator.domain.CalcKey
import com.desklauncher.feature.calculator.ui.component.CalcKeypad

@Composable
fun CalculatorRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalculatorScreen(state = uiState, onKey = viewModel::press, onBack = onBack, modifier = modifier)
}

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onKey: (CalcKey) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.calculator_tool_title),
                tabColor = DeskTabColor.Teal,
                backContentDescription = stringResource(R.string.calculator_back),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).navigationBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
            ) {
                // 길어질수록 글자를 줄인다
                val size = when {
                    state.display.length > 18 -> 28.sp
                    state.display.length > 12 -> 36.sp
                    else -> 48.sp
                }
                Text(
                    text = state.display.ifEmpty { "0" },
                    fontSize = size,
                    lineHeight = size,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                val secondary = when (state.error) {
                    CalcError.Syntax -> stringResource(R.string.calculator_error_syntax)
                    CalcError.DivideByZero -> stringResource(R.string.calculator_error_divide_by_zero)
                    null -> state.preview?.let { "= $it" }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = secondary.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    IconButton(onClick = { onKey(CalcKey.Backspace) }) {
                        Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = stringResource(R.string.calculator_key_backspace))
                    }
                }
            }
            CalcKeypad(onKey = onKey)
        }
    }
}

@DeskPreviews
@Composable
private fun CalculatorScreenPreview() {
    DeskTheme { CalculatorScreen(CalculatorUiState(display = "1,250×(3+4", preview = "8,750"), onKey = {}, onBack = {}) }
}

@DeskPreviews
@Composable
private fun CalculatorScreenErrorPreview() {
    DeskTheme { CalculatorScreen(CalculatorUiState(display = "5÷0", error = CalcError.DivideByZero), onKey = {}, onBack = {}) }
}
