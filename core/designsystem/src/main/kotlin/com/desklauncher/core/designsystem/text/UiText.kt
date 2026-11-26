package com.desklauncher.core.designsystem.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

/**
 * ViewModel이 Context 없이 화면 문자열을 전달하기 위한 타입.
 * 실제 화면은 [Res]를, Preview 가짜 데이터는 [Plain]을 쓴다.
 */
@Immutable
sealed interface UiText {
    data class Res(@StringRes val id: Int) : UiText
    data class Plain(val value: String) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Res -> stringResource(id)
    is UiText.Plain -> value
}
