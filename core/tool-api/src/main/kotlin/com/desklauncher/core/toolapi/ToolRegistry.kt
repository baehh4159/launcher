package com.desklauncher.core.toolapi

import com.desklauncher.core.designsystem.theme.DeskTabColor
import kotlinx.coroutines.flow.Flow

/**
 * 책상 도구 레지스트리. 셸(책상)과 도구함이 함께 사용한다.
 * 도구를 꺼도 그 도구의 데이터는 지우지 않는다 (PRD §6.3).
 */
interface ToolRegistry {

    /** 코드에 등록된 모든 도구 (켜짐/꺼짐 무관). 내비게이션 그래프 등록에 사용. */
    val registeredTools: List<DeskTool>

    /** 도구함용: 등록된 모든 도구 + 설정, 탭 순서대로. */
    val allTools: Flow<List<ActiveTool>>

    /** 책상 인덱스 탭용: 켜진 도구만, 탭 순서대로. */
    val enabledTools: Flow<List<ActiveTool>>

    fun findTool(toolId: String): DeskTool?

    suspend fun setEnabled(toolId: String, enabled: Boolean)

    /** [orderedToolIds] 순서대로 탭 순서를 다시 매긴다. */
    suspend fun reorder(orderedToolIds: List<String>)

    /** null이면 기본 색으로 되돌린다. */
    suspend fun setTabColor(toolId: String, tabColor: DeskTabColor?)
}
