package com.desklauncher.core.designsystem.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

/**
 * 서류철 열기/닫기 전환 (SHELL-05, PRD §3.2 Opening).
 * 열기: 우측 서류철이 책상 위로 '꺼내져' 펼쳐짐. 닫기: 오른쪽으로 다시 '꽂힘'.
 * 닫기(pop) 전환은 예측형 뒤로가기 제스처 진행률에 따라 Navigation이 직접 재생한다.
 */
object DeskTransitions {

    /** 서류철이 꽂혀 있는 위치(우측, 하단 2/3 가운데쯤). */
    private val FolderOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.62f)

    private const val OpenMillis = 260
    private const val CloseMillis = 220

    private val slideSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

    val folderOpen: EnterTransition =
        slideInHorizontally(slideSpring) { fullWidth -> fullWidth / 2 } +
            scaleIn(tween(OpenMillis, easing = FastOutSlowInEasing), initialScale = 0.86f, transformOrigin = FolderOrigin) +
            fadeIn(tween(OpenMillis / 2))

    val folderClose: ExitTransition =
        slideOutHorizontally(tween(CloseMillis, easing = FastOutSlowInEasing)) { fullWidth -> fullWidth / 2 } +
            scaleOut(tween(CloseMillis), targetScale = 0.86f, transformOrigin = FolderOrigin) +
            fadeOut(tween(CloseMillis))

    /** 서류철이 열릴 때 책상은 살짝 뒤로 물러나며 어두워진다. */
    val deskRecede: ExitTransition =
        scaleOut(tween(OpenMillis), targetScale = 0.96f) + fadeOut(tween(OpenMillis), targetAlpha = 0.5f)

    val deskReturn: EnterTransition =
        scaleIn(tween(CloseMillis), initialScale = 0.96f) + fadeIn(tween(CloseMillis), initialAlpha = 0.5f)

    /** 앱 서랍: 아래에서 올라오고 아래로 내려간다. */
    val drawerOpen: EnterTransition = slideInVertically(slideSpring) { it / 3 } + fadeIn(tween(OpenMillis / 2))
    val drawerClose: ExitTransition = slideOutVertically(tween(CloseMillis)) { it / 3 } + fadeOut(tween(CloseMillis))

    /** 도구 내부 화면 이동 (책상과 무관). */
    val innerEnter: EnterTransition = slideInHorizontally(slideSpring) { it / 3 } + fadeIn(tween(180))
    val innerExit: ExitTransition = fadeOut(tween(120))
    val innerPopEnter: EnterTransition = fadeIn(tween(180))
    val innerPopExit: ExitTransition = slideOutHorizontally(tween(CloseMillis)) { it / 3 } + fadeOut(tween(CloseMillis))
}
