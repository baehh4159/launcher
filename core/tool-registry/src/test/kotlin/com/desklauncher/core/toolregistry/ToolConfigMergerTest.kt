package com.desklauncher.core.toolregistry

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.database.entity.ToolConfigEntity
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolConfigMergerTest {

    private val todo = FakeTool(id = "todo", defaultOrder = 0, defaultTabColor = DeskTabColor.Orange)
    private val memo = FakeTool(id = "memo", defaultOrder = 1, defaultTabColor = DeskTabColor.Blue)
    private val alarm = FakeTool(id = "alarm", defaultOrder = 2, defaultEnabled = false)

    // DB에 설정이 없으면 도구 기본값을 쓴다
    @Test
    fun merge_usesToolDefaults_whenNoConfig() {
        val result = ToolConfigMerger.merge(listOf(todo, memo, alarm), emptyList())

        assertEquals(listOf("todo", "memo", "alarm"), result.map { it.id })
        assertEquals(listOf(true, true, false), result.map { it.enabled })
        assertEquals(DeskTabColor.Orange, result.first().tabColor)
    }

    // DB 설정의 순서·켜짐·색상을 우선한다
    @Test
    fun merge_prefersStoredConfig() {
        val configs = listOf(
            ToolConfigEntity(toolId = "todo", enabled = false, sortOrder = 2, tabColor = "Green"),
            ToolConfigEntity(toolId = "memo", enabled = true, sortOrder = 0, tabColor = null),
            ToolConfigEntity(toolId = "alarm", enabled = true, sortOrder = 1, tabColor = null),
        )

        val result = ToolConfigMerger.merge(listOf(todo, memo, alarm), configs)

        assertEquals(listOf("memo", "alarm", "todo"), result.map { it.id })
        assertEquals(listOf(true, true, false), result.map { it.enabled })
        assertEquals(DeskTabColor.Green, result.last().tabColor)
        assertEquals(DeskTabColor.Blue, result.first().tabColor)
    }

    // 코드에서 사라진 도구의 설정은 무시한다
    @Test
    fun merge_ignoresConfigOfRemovedTool() {
        val configs = listOf(ToolConfigEntity(toolId = "removed", enabled = true, sortOrder = 0, tabColor = null))

        val result = ToolConfigMerger.merge(listOf(todo), configs)

        assertEquals(listOf("todo"), result.map { it.id })
    }

    // 알 수 없는 색상 이름은 기본 색으로 대체한다
    @Test
    fun merge_fallsBackToDefaultColor_whenStoredColorUnknown() {
        val configs = listOf(ToolConfigEntity(toolId = "todo", enabled = true, sortOrder = 0, tabColor = "Unknown"))

        val result = ToolConfigMerger.merge(listOf(todo), configs)

        assertEquals(DeskTabColor.Orange, result.single().tabColor)
    }

    // 기본 설정은 도구 기본값으로 만들고 사용자 색은 비워 둔다
    @Test
    fun defaultConfigs_useToolDefaultsWithoutCustomColor() {
        val defaults = ToolConfigMerger.defaultConfigs(listOf(todo, alarm))

        assertEquals(
            listOf(
                ToolConfigEntity(toolId = "todo", enabled = true, sortOrder = 0, tabColor = null),
                ToolConfigEntity(toolId = "alarm", enabled = false, sortOrder = 2, tabColor = null),
            ),
            defaults,
        )
    }

    private class FakeTool(
        override val id: String,
        override val defaultOrder: Int,
        override val defaultEnabled: Boolean = true,
        override val defaultTabColor: DeskTabColor = DeskTabColor.Gray,
    ) : DeskTool {
        override val titleRes: Int = 0
        override val descriptionRes: Int = 0
        override val icon: ImageVector get() = error("테스트에서 사용하지 않음")
        override val startDestination: Any = Unit
        override fun NavGraphBuilder.toolGraph(navController: NavController) = Unit
    }
}
