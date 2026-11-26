package com.desklauncher.core.toolregistry

import com.desklauncher.core.database.entity.ToolConfigEntity
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.ActiveTool
import com.desklauncher.core.toolapi.DeskTool

/** 코드에 등록된 도구와 DB 설정을 합치는 순수 함수 모음 (PRD §6.3). */
internal object ToolConfigMerger {

    /** 최초 설치 시 넣을 기본 설정. 이미 있는 행은 DAO가 무시한다. */
    fun defaultConfigs(tools: Collection<DeskTool>): List<ToolConfigEntity> =
        tools.map { tool ->
            ToolConfigEntity(
                toolId = tool.id,
                enabled = tool.defaultEnabled,
                sortOrder = tool.defaultOrder,
                tabColor = null,
            )
        }

    /**
     * - 코드에 있고 DB에 없는 도구 → 기본값으로 포함
     * - DB에 있고 코드에 없는 도구(삭제된 모듈) → 무시 (행은 보존)
     * - 정렬: sortOrder → defaultOrder → id
     */
    fun merge(tools: Collection<DeskTool>, configs: List<ToolConfigEntity>): List<ActiveTool> {
        val configById = configs.associateBy { it.toolId }
        return tools
            .map { tool ->
                val config = configById[tool.id]
                ActiveTool(
                    tool = tool,
                    enabled = config?.enabled ?: tool.defaultEnabled,
                    sortOrder = config?.sortOrder ?: tool.defaultOrder,
                    tabColor = DeskTabColor.fromNameOrNull(config?.tabColor) ?: tool.defaultTabColor,
                )
            }
            .sortedWith(compareBy<ActiveTool>({ it.sortOrder }, { it.tool.defaultOrder }, { it.id }))
    }
}
