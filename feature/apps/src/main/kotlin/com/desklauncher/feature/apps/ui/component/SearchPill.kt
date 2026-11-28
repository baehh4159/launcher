package com.desklauncher.feature.apps.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSize
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.apps.R

/** 앱 서랍 하단 검색 알약 — 한 손 엄지가 닿는 곳 (PRD §19 한 손 도달) */
@Composable
fun SearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeskTheme.colors
    // 입력칸 상태는 여기서 들고(한글 조합 보존), 바뀐 글자만 위로 알린다
    val fieldState = rememberTextFieldState(query)
    val currentOnChange by rememberUpdatedState(onQueryChange)
    LaunchedEffect(fieldState) {
        snapshotFlow { fieldState.text.toString() }.collect { currentOnChange(it) }
    }
    // 바깥에서 비운 경우(앱 실행 후 등)에만 맞춘다 — 조합 중인 글자를 덮어쓰지 않게
    LaunchedEffect(query) {
        if (query.isEmpty() && fieldState.text.isNotEmpty()) fieldState.clearText()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DeskSize.control)
            .background(colors.desk, DeskShape.pill)
            .border(DeskLine.thin, colors.lineStrong, DeskShape.pill)
            .padding(start = DeskSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DeskSpacing.xs),
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.muted, modifier = Modifier.size(DeskSize.icon))
        Box(Modifier.weight(1f)) {
            if (fieldState.text.isEmpty()) {
                Text(stringResource(R.string.apps_search_hint), style = DeskType.label, color = colors.muted, maxLines = 1)
            }
            BasicTextField(
                state = fieldState,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = DeskType.body.copy(color = colors.onPaper),
                cursorBrush = SolidColor(colors.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                onKeyboardAction = { onSubmit() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (fieldState.text.isNotEmpty()) {
            IconButton(onClick = { fieldState.clearText() }) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.apps_search_clear), tint = colors.muted)
            }
        }
    }
}

@DeskFontScalePreviews
@Composable
private fun SearchPillPreview() {
    DeskTheme { SearchPill(query = "", onQueryChange = {}, onSubmit = {}, modifier = Modifier.padding(DeskSpacing.m)) }
}
