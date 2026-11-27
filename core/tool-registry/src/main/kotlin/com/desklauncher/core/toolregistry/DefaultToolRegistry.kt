package com.desklauncher.core.toolregistry

import android.util.Log
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

    /** 검사를 통과한 도구만 싣는다. Set 순회 순서가 바뀌어도 중복 처리 결과가 같도록 클래스 이름으로 먼저 정렬한다. */
    override val registeredTools: List<DeskTool> = run {
        val ordered = tools.sortedBy { it::class.java.name }
        val result = ToolValidator.validate(
            ordered.map { ToolValidator.Candidate(id = it.id, apiVersion = it.apiVersion, source = it::class.java.name) },
        )
        result.rejected.forEach { Log.w(TAG, "도구를 싣지 않음: $it") }
        result.accepted.map(ordered::get).sortedWith(compareBy(DeskTool::defaultOrder, DeskTool::id))
    }

    private val toolById: Map<String, DeskTool> = registeredTools.associateBy { it.id }

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

    private companion object {
        const val TAG = "ToolRegistry"
    }
}
