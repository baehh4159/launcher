package com.desklauncher.feature.doodle.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskPenColor
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.doodle.R
import com.desklauncher.feature.doodle.domain.DoodleState
import com.desklauncher.feature.doodle.domain.EraserWidthDp
import com.desklauncher.feature.doodle.domain.PenWidth
import com.desklauncher.feature.doodle.domain.Stroke
import com.desklauncher.feature.doodle.ui.canvas.DoodleCanvas
import com.desklauncher.feature.doodle.ui.component.DoodleToolbar

@Composable
fun DoodleEditorRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoodleEditorViewModel = hiltViewModel(),
) {
    DoodleEditorScreen(
        doodle = viewModel.doodle,
        color = viewModel.color,
        width = viewModel.width,
        eraser = viewModel.eraser,
        onStroke = viewModel::addStroke,
        onColor = viewModel::selectColor,
        onWidth = viewModel::selectWidth,
        onEraser = viewModel::selectEraser,
        onBack = onBack,
        modifier = modifier,
        topBarActions = {
            HistoryActions(
                canUndo = viewModel.doodle.canUndo,
                canRedo = viewModel.doodle.canRedo,
                canClear = !viewModel.doodle.isEmpty,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onClear = viewModel::clear,
            )
        },
    )
}

/** 실행 취소 · 다시 실행 · 전체 지우기 (DOODLE-02) */
@Composable
internal fun HistoryActions(
    canUndo: Boolean,
    canRedo: Boolean,
    canClear: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
) {
    IconButton(onClick = onUndo, enabled = canUndo) {
        Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = stringResource(R.string.doodle_undo))
    }
    IconButton(onClick = onRedo, enabled = canRedo) {
        Icon(Icons.AutoMirrored.Outlined.Redo, contentDescription = stringResource(R.string.doodle_redo))
    }
    IconButton(onClick = onClear, enabled = canClear) {
        Icon(Icons.Outlined.LayersClear, contentDescription = stringResource(R.string.doodle_clear))
    }
}

@Composable
fun DoodleEditorScreen(
    doodle: DoodleState,
    color: DeskPenColor,
    width: PenWidth,
    eraser: Boolean,
    onStroke: (Stroke) -> Unit,
    onColor: (DeskPenColor) -> Unit,
    onWidth: (PenWidth) -> Unit,
    onEraser: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.doodle_tool_title),
                tabColor = DeskTabColor.Purple,
                backContentDescription = stringResource(R.string.doodle_back),
                onBack = onBack,
                actions = { topBarActions() },
            )
        },
        bottomBar = {
            DoodleToolbar(
                color = color,
                width = width,
                eraser = eraser,
                onColor = onColor,
                onWidth = onWidth,
                onEraser = onEraser,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            DoodleCanvas(
                strokes = doodle.strokes,
                newStroke = {
                    val widthPx = with(density) { (if (eraser) EraserWidthDp else width.dp).dp.toPx() }
                    Stroke(argb = color.color.toArgb(), widthPx = widthPx, eraser = eraser)
                },
                onStrokeFinished = onStroke,
                contentDescription = stringResource(R.string.doodle_canvas),
            )
        }
    }
}

@DeskPreviews
@Composable
private fun DoodleEditorScreenPreview() {
    DeskTheme {
        DoodleEditorScreen(
            doodle = DoodleState(listOf(Stroke(DeskPenColor.Blue.color.toArgb(), 12f, points = listOf(100f, 200f, 300f, 400f, 500f, 260f)))),
            color = DeskPenColor.Blue, width = PenWidth.Medium, eraser = false,
            onStroke = {}, onColor = {}, onWidth = {}, onEraser = {}, onBack = {},
            topBarActions = { HistoryActions(canUndo = true, canRedo = false, canClear = true, onUndo = {}, onRedo = {}, onClear = {}) },
        )
    }
}
