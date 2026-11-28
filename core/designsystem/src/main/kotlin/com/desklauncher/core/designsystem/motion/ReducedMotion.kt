package com.desklauncher.core.designsystem.motion

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * 움직임 줄이기 상태 (DESIGN_LANGUAGE D3 "Keep meaning. Remove travel.").
 * 시스템 '애니메이션 제거'(애니메이터 길이 배율 0)이면 true — 이동·크기·스프링 대신 짧은 페이드만 쓴다.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
internal fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
