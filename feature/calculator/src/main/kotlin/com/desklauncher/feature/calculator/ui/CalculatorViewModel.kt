package com.desklauncher.feature.calculator.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.calculator.domain.CalcError
import com.desklauncher.feature.calculator.domain.CalcKey
import com.desklauncher.feature.calculator.domain.CalculatorInput
import com.desklauncher.feature.calculator.domain.Expression
import com.desklauncher.feature.calculator.domain.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Immutable
data class CalculatorUiState(
    /** 화면에 보일 수식 (숫자에 천 단위 쉼표) */
    val display: String = "",
    /** 실시간 미리보기 결과 */
    val preview: String? = null,
    val error: CalcError? = null,
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val input = savedStateHandle.getStateFlow(InputKey, CalculatorInput())

    val uiState: StateFlow<CalculatorUiState> = input.map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CalculatorUiState())

    fun press(key: CalcKey) {
        savedStateHandle[InputKey] = input.value.press(key)
    }

    private companion object {
        const val InputKey = "input"

        fun CalculatorInput.toUiState() = CalculatorUiState(
            display = groupNumbers(expression),
            preview = if (justEvaluated) null else Expression.preview(expression)?.let(NumberFormatter::format)?.replace('-', '−'),
            error = error,
        )

        /** 수식 안의 각 숫자 정수부에 천 단위 쉼표 */
        fun groupNumbers(expression: String): String =
            Regex("\\d+(\\.\\d*)?").replace(expression) { match ->
                val integer = match.value.substringBefore('.')
                val rest = match.value.drop(integer.length)
                integer.reversed().chunked(3).joinToString(",").reversed() + rest
            }
    }
}
