package com.desklauncher.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.desklauncher.R
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.toolTabColor
import com.desklauncher.core.toolapi.DeskToolHost
import com.desklauncher.core.toolapi.PluginStorage
import com.desklauncher.core.toolapi.PluginStorageProvider
import com.desklauncher.core.toolapi.SimpleDeskTool
import com.desklauncher.core.toolapi.SimpleToolDestination
import com.desklauncher.core.toolapi.ToolRegistry

/** [SimpleDeskTool] 공용 목적지. 라우트의 toolId로 도구를 찾아 호스트를 만들어 건넨다. */
internal fun NavGraphBuilder.simpleToolScreen(
    toolRegistry: ToolRegistry,
    pluginStorageProvider: PluginStorageProvider,
    onClose: () -> Unit,
) {
    composable<SimpleToolDestination> { entry ->
        val toolId = entry.toRoute<SimpleToolDestination>().toolId
        val tool = toolRegistry.findTool(toolId) as? SimpleDeskTool
        if (tool == null) {
            // 플러그인 모듈을 뺀 뒤 남은 라우트 등 — 크래시 대신 안내 (런처 안정성, AGENTS.md §5)
            MissingToolScreen(onClose = onClose)
            return@composable
        }
        val tabColor = toolTabColor(tool.id, tool.defaultTabColor)
        val latestClose = rememberUpdatedState(onClose)
        val storage = remember(tool.id) { pluginStorageProvider.forTool(tool.id) }
        val host = remember(tool.id, tabColor, storage) {
            AppDeskToolHost(toolId = tool.id, tabColor = tabColor, storage = storage, onClose = { latestClose.value() })
        }
        tool.Content(host = host, modifier = Modifier.fillMaxSize())
    }
}

private class AppDeskToolHost(
    override val toolId: String,
    override val tabColor: DeskTabColor,
    override val storage: PluginStorage,
    private val onClose: () -> Unit,
) : DeskToolHost {
    override fun close() = onClose()
}

@Composable
internal fun MissingToolScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    DeskEmptyState(
        title = stringResource(R.string.missing_tool_title),
        body = stringResource(R.string.missing_tool_body),
        modifier = modifier.fillMaxSize().wrapContentSize().padding(24.dp),
        action = { FilledTonalButton(onClick = onClose) { Text(stringResource(R.string.missing_tool_back)) } },
    )
}

@DeskPreviews
@Composable
private fun MissingToolScreenPreview() {
    DeskTheme { MissingToolScreen(onClose = {}) }
}
