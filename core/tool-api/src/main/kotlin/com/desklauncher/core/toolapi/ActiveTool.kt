package com.desklauncher.core.toolapi

import com.desklauncher.core.designsystem.theme.DeskTabColor

/** 레지스트리가 코드에 등록된 [DeskTool]과 사용자 설정(`tool_config`)을 합친 결과. */
data class ActiveTool(
    val tool: DeskTool,
    val enabled: Boolean,
    val sortOrder: Int,
    /** 사용자 지정 색이 없으면 [DeskTool.defaultTabColor]. */
    val tabColor: DeskTabColor,
) {
    val id: String get() = tool.id
}
