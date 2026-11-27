package com.desklauncher.core.toolapi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 플러그인 화면 기본 틀: 탭 색 서류철 헤더 + 뒤로(책상으로) + 본문.
 * 모든 도구가 같은 머리를 갖게 해 책상 안에서 이질감이 없도록 한다.
 */
@Composable
fun DeskToolScaffold(
    host: DeskToolHost,
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = title,
                tabColor = host.tabColor,
                backContentDescription = stringResource(R.string.tool_api_back_to_desk),
                onBack = host::close,
                actions = actions,
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

/** Preview·테스트용 가짜 호스트 (메모리 저장소) */
class PreviewDeskToolHost(
    override val toolId: String = "preview",
    override val tabColor: DeskTabColor = DeskTabColor.Green,
) : DeskToolHost {
    override val storage: PluginStorage = InMemoryPluginStorage()
    override fun close() = Unit
}

/** 메모리 저장소 — Preview와 단위 테스트에서 [PluginStorage] 대신 쓴다 */
class InMemoryPluginStorage(initial: Map<String, String> = emptyMap()) : PluginStorage {
    private val state = MutableStateFlow(initial)
    override fun observe(key: String): Flow<String?> = state.map { it[key] }
    override fun observeAll(): Flow<Map<String, String>> = state
    override suspend fun get(key: String): String? = state.value[key]
    override suspend fun put(key: String, value: String?) {
        state.value = if (value == null) state.value - key else state.value + (key to value)
    }
    override suspend fun clear() {
        state.value = emptyMap()
    }
}

@DeskPreviews
@Composable
private fun DeskToolScaffoldPreview() {
    DeskTheme {
        DeskToolScaffold(host = PreviewDeskToolHost(), title = "물 한 잔") { padding ->
            Text("플러그인 본문", modifier = Modifier.padding(padding))
        }
    }
}
