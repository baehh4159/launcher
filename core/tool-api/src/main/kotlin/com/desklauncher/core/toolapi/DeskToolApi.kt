package com.desklauncher.core.toolapi

/**
 * 플러그인 SDK 버전과 규칙.
 * - [VERSION]: 계약이 호환되지 않게 바뀔 때만 올린다. 플러그인은 [DeskTool.apiVersion]으로 자기가 맞춘 버전을 밝힌다.
 *   앱은 1..[VERSION] 범위의 플러그인만 싣는다 (더 새 SDK로 만든 플러그인은 제외 — 앱을 먼저 업데이트).
 * - [IdPattern]: 도구 id 규칙. 소문자로 시작, 소문자·숫자·밑줄 2~32자. DB 키라서 출시 후 바꾸지 않는다.
 */
object DeskToolApi {
    const val VERSION: Int = 1
    val IdPattern: Regex = Regex("[a-z][a-z0-9_]{1,31}")
}
