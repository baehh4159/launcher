package com.desklauncher.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.motion.LocalReducedMotion

/** 누름 피드백: 0.97배로 살짝 들어간다 (beUI whileTap, DESIGN_LANGUAGE §4). 움직임 줄이기면 크기 변화 없음. */
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) DeskMotion.PressScale else 1f,
        animationSpec = DeskMotion.press(),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
