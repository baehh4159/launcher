package com.desklauncher.plugin.water.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.toolapi.DeskToolHost
import com.desklauncher.core.toolapi.DeskToolScaffold
import com.desklauncher.core.toolapi.PreviewDeskToolHost
import com.desklauncher.plugin.water.R
import com.desklauncher.plugin.water.data.WaterStore
import com.desklauncher.plugin.water.ui.preview.WaterPreviewData
import java.time.LocalDate

@Composable
fun WaterRoute(host: DeskToolHost, modifier: Modifier = Modifier) {
    // 호스트가 준 전용 저장소로 ViewModel을 만든다 — 플러그인은 앱 DI 그래프를 몰라도 된다.
    val viewModel: WaterViewModel = viewModel(key = host.toolId) {
        WaterViewModel(store = WaterStore(host.storage), today = LocalDate::now)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WaterScreen(state = state, onAction = viewModel::onAction, host = host, modifier = modifier)
}

@Composable
fun WaterScreen(
    state: WaterUiState,
    onAction: (WaterAction) -> Unit,
    host: DeskToolHost,
    modifier: Modifier = Modifier,
) {
    DeskToolScaffold(host = host, title = stringResource(R.string.water_tool_title), modifier = modifier) { padding ->
        when (state) {
            WaterUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is WaterUiState.Ready -> WaterContent(
                state = state,
                onAction = onAction,
                host = host,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WaterContent(
    state: WaterUiState.Ready,
    onAction: (WaterAction) -> Unit,
    host: DeskToolHost,
    modifier: Modifier = Modifier,
) {
    val day = state.today
    val colors = DeskTheme.colors
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = stringResource(R.string.water_count, day.cups, day.goal),
            style = MaterialTheme.typography.displayMedium,
            color = colors.onPaper,
        )
        Text(
            text = stringResource(if (day.reachedGoal) R.string.water_goal_reached else R.string.water_goal_left, (day.goal - day.cups).coerceAtLeast(0)),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onDeskSecondary,
        )
        // 목표만큼 동그라미 — 마신 만큼 탭 색으로 채운다
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(maxOf(day.goal, day.cups)) { index ->
                val filled = index < day.cups
                Box(
                    Modifier
                        .size(28.dp)
                        .border(DeskLine.Thin, colors.lineStrong, CircleShape)
                        .background(if (filled) host.tabColor.container else colors.paper, CircleShape),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onAction(WaterAction.Undo) }, enabled = day.cups > 0) {
                Text(stringResource(R.string.water_undo))
            }
            Button(
                onClick = { onAction(WaterAction.Drink) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = host.tabColor.container,
                    contentColor = host.tabColor.onContainer,
                ),
            ) {
                Text(stringResource(R.string.water_drink))
            }
        }
    }
}

@DeskPreviews
@Composable
private fun WaterScreenPreview() {
    DeskTheme {
        WaterScreen(state = WaterPreviewData.halfway, onAction = {}, host = PreviewDeskToolHost(toolId = "water"))
    }
}

@DeskPreviews
@Composable
private fun WaterScreenEmptyPreview() {
    DeskTheme {
        WaterScreen(state = WaterPreviewData.empty, onAction = {}, host = PreviewDeskToolHost(toolId = "water"))
    }
}

@DeskPreviews
@Composable
private fun WaterScreenLoadingPreview() {
    DeskTheme {
        WaterScreen(state = WaterUiState.Loading, onAction = {}, host = PreviewDeskToolHost(toolId = "water"))
    }
}
