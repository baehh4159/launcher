package com.desklauncher.core.toolregistry

import com.desklauncher.core.database.dao.ToolConfigDao
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.ActiveTool
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.core.toolapi.ToolRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultToolRegistry @Inject constructor(
    tools: Set<@JvmSuppressWildcards DeskTool>,
    private val toolConfigDao: ToolConfigDao,
) : ToolRegistry {

    init {
        val duplicated = tools.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicated.isEmpty()) { "DeskTool.id 중복: $duplicated" }
    }

    override val registeredTools: List<DeskTool> = tools.sortedBy { it.defaultOrder }

    private val toolById: Map<String, DeskTool> = tools.associateBy { it.id }

    override val allTools: Flow<List<ActiveTool>> = flow {
        // 새로 추가된 도구만 기본값으로 시드한다 (기존 사용자 설정은 IGNORE로 보존).
        toolConfigDao.insertIfAbsent(ToolConfigMerger.defaultConfigs(registeredTools))
        emitAll(toolConfigDao.observeAll().map { configs -> ToolConfigMerger.merge(registeredTools, configs) })
    }

    override val enabledTools: Flow<List<ActiveTool>> =
        allTools.map { tools -> tools.filter(ActiveTool::enabled) }

    override fun findTool(toolId: String): DeskTool? = toolById[toolId]

    override suspend fun setEnabled(toolId: String, enabled: Boolean) {
        toolConfigDao.setEnabled(toolId, enabled)
    }

    override suspend fun reorder(orderedToolIds: List<String>) {
        toolConfigDao.reorder(orderedToolIds)
    }

    override suspend fun setTabColor(toolId: String, tabColor: DeskTabColor?) {
        toolConfigDao.setTabColor(toolId, tabColor?.name)
    }
}
