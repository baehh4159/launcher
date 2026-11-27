package com.desklauncher.core.toolapi

import com.desklauncher.core.designsystem.theme.DeskTabColor

/**
 * 앱(책상)이 플러그인에게 빌려주는 기능. 플러그인은 앱 내부 구현을 몰라도 이것만으로 동작한다.
 * 기능을 늘릴 때는 기본 구현이 있는 멤버로만 추가해 기존 플러그인이 깨지지 않게 한다.
 */
interface DeskToolHost {
    val toolId: String

    /** 도구함에서 사용자가 고른 이 도구의 탭 색 (없으면 기본 색) */
    val tabColor: DeskTabColor

    /** 이 플러그인 전용 저장소 */
    val storage: PluginStorage

    /** 도구를 닫고 책상으로 돌아간다 */
    fun close()
}
