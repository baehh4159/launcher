package com.desklauncher.core.testing

import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.ActiveTool
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.core.toolapi.ToolRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/** 메모리 레지스트리. [emit] 전까지는 값을 내지 않는다(로딩 상태 재현). */
class FakeToolRegistry : ToolRegistry {

    private val tools = MutableStateFlow<List<ActiveTool>?>(null)

    fun emit(vararg activeTools: ActiveTool) {
        tools.value = activeTools.toList().sortedBy { it.sortOrder }
    }

    val current: List<ActiveTool> get() = tools.value.orEmpty()

    override val registeredTools: List<DeskTool> get() = current.map { it.tool }
    override val allTools: Flow<List<ActiveTool>> = tools.filterNotNull()
    override val enabledTools: Flow<List<ActiveTool>> = allTools.map { list -> list.filter { it.enabled } }

    override fun findTool(toolId: String): DeskTool? = registeredTools.firstOrNull { it.id == toolId }

    override suspend fun setEnabled(toolId: String, enabled: Boolean) {
        update(toolId) { it.copy(enabled = enabled) }
    }

    override suspend fun reorder(orderedToolIds: List<String>) {
        val byId = current.associateBy { it.id }
        tools.value = orderedToolIds.mapIndexedNotNull { index, id -> byId[id]?.copy(sortOrder = index) }
    }

    override suspend fun setTabColor(toolId: String, tabColor: DeskTabColor?) {
        update(toolId) { it.copy(tabColor = tabColor ?: it.tool.defaultTabColor) }
    }

    private fun update(toolId: String, transform: (ActiveTool) -> ActiveTool) {
        tools.value = current.map { if (it.id == toolId) transform(it) else it }
    }
}

fun activeTool(
    id: String,
    order: Int,
    enabled: Boolean = true,
    color: DeskTabColor = DeskTabColor.Gray,
): ActiveTool = ActiveTool(
    tool = FakeDeskTool(id = id, defaultOrder = order, defaultTabColor = color),
    enabled = enabled,
    sortOrder = order,
    tabColor = color,
)
