package com.desklauncher.core.datastore

/** 앱 설정 (PRD §13 설정·DataStore). 순수 Kotlin 모델. */
data class DeskSettings(
    val haptics: HapticSetting = HapticSetting.Full,
    val stripLayout: StripLayout = StripLayout.Split,
    /** 첫 실행 훑기 안내를 본 뒤 한 번이라도 훑기로 열었는지 (PRD §4.2 첫 실행 안내) */
    val scrubHintDone: Boolean = false,
)

/** 촉각 설정 (PRD §5). 저장 값은 이름 문자열이므로 이름을 바꾸지 않는다. */
enum class HapticSetting { Full, ConfirmOnly, Off }

/**
 * 서류철 배치 (PRD §17 실험). [Split]이 출시 기본값,
 * [RightVisible]은 비교 실험용(STRIP-04, 개발용 토글).
 */
enum class StripLayout { Split, RightVisible }
