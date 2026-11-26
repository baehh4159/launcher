package com.desklauncher.core.designsystem.scrub

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.desklauncher.core.designsystem.motion.DeskMotion
import kotlin.math.abs

/** PRD §3.2 상태 머신. Flinging은 Settling에 포함된다. */
enum class ScrubPhase {
    /** 탭이 가장자리에 살짝만 보임 */
    Idle,

    /** 손가락으로 훑는 중 (1:1 추적) */
    Scrubbing,

    /** 손을 뗀 뒤 정수 인덱스로 스냅/플링 중 */
    Settling,

    /** 플링이 멈춘 뒤 포커스 유지 — 탭하면 열림, 시간이 지나면 Idle */
    Selected,
}

/**
 * 인덱스 스크럽 상태. 서류철 스트립과 앱 서랍 초성 스트립이 함께 쓴다.
 * 드래그 중에는 [dragFocus]를 직접 쓰고, 손을 떼면 [animatable]로 이어받는다 —
 * 매 이동마다 코루틴을 띄우지 않기 위함.
 */
@Stable
class IndexScrubState(
    initialFocus: Float = 0f,
    initialPhase: ScrubPhase = ScrubPhase.Idle,
) {
    private val animatable = Animatable(initialFocus)
    private var dragFocus by mutableFloatStateOf(initialFocus)
    private var isDragging by mutableStateOf(false)

    var phase: ScrubPhase by mutableStateOf(initialPhase)
        private set

    /** 손가락이 스트립에서 충분히 벗어나 손을 떼면 취소될 상태. */
    var isCancelArmed: Boolean by mutableStateOf(false)
        private set

    var itemCount: Int by mutableIntStateOf(0)
        internal set

    /** 연속 포커스(탭 단위). 프레임마다 바뀌므로 Composition이 아닌 레이아웃/그리기 단계에서 읽는다. */
    val focus: Float
        get() = if (isDragging) dragFocus else animatable.value

    /** 가장 가까운 탭. 정수가 바뀔 때만 갱신되므로 Composition에서 읽어도 된다. */
    val focusedIndex: Int by derivedStateOf { IndexScrubMath.nearestIndex(focus, itemCount) }

    internal fun beginDrag(focus: Float) {
        isDragging = true
        dragFocus = focus
        isCancelArmed = false
        phase = ScrubPhase.Scrubbing
    }

    internal fun drag(focus: Float, cancelArmed: Boolean) {
        dragFocus = focus
        isCancelArmed = cancelArmed
    }

    internal suspend fun stopAnimation() = animatable.stop()

    /**
     * 손을 뗐을 때 호출. [velocity]는 탭/초.
     * - 취소 상태: 스냅 후 Idle
     * - 느리게 놓음: 그 탭을 바로 [onOpen] (반응성 우선) 후 스냅 → Idle
     * - 플링: 넘겨서 멈춘 뒤 Selected
     */
    internal suspend fun release(
        velocity: Float,
        onOpen: (index: Int) -> Unit,
        spec: AnimationSpec<Float> = DeskMotion.SettleSpring,
    ) {
        animatable.snapTo(dragFocus)
        isDragging = false
        val count = itemCount
        if (count == 0) {
            phase = ScrubPhase.Idle
            return
        }
        if (isCancelArmed) {
            phase = ScrubPhase.Settling
            isCancelArmed = false
            animatable.animateTo(IndexScrubMath.nearestIndex(dragFocus, count).toFloat(), spec)
            phase = ScrubPhase.Idle
            return
        }
        if (abs(velocity) < DeskMotion.FlingVelocityThreshold) {
            val target = IndexScrubMath.nearestIndex(dragFocus, count)
            phase = ScrubPhase.Selected
            onOpen(target)
            animatable.animateTo(target.toFloat(), spec, initialVelocity = velocity)
            phase = ScrubPhase.Idle
            return
        }
        phase = ScrubPhase.Settling
        val target = IndexScrubMath.flingTarget(dragFocus, velocity, count)
        animatable.animateTo(target.toFloat(), spec, initialVelocity = velocity)
        phase = ScrubPhase.Selected
    }

    /** 홈 버튼·뒤로가기·시간 초과 시 Idle로. */
    suspend fun reset() {
        animatable.stop()
        isDragging = false
        isCancelArmed = false
        phase = ScrubPhase.Idle
    }
}

@Composable
fun rememberIndexScrubState(
    initialFocus: Float = 0f,
    initialPhase: ScrubPhase = ScrubPhase.Idle,
): IndexScrubState = remember { IndexScrubState(initialFocus, initialPhase) }
