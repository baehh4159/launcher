package com.desklauncher.core.designsystem.scrub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.desklauncher.core.designsystem.haptics.DeskHaptics
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics

/** 스크럽 포커스 변화를 햅틱으로 바꾼다 (SHELL-04). 잡는 순간 1회 + 경계마다 1회 + 벽. */
@Composable
fun IndexScrubHaptics(
    state: IndexScrubState,
    haptics: DeskHaptics = rememberDeskHaptics(),
) {
    LaunchedEffect(state, haptics) {
        val tracker = ScrubHapticTracker()
        var previousPhase = state.phase
        snapshotFlow { state.phase to state.focus }.collect { (phase, focus) ->
            val count = state.itemCount
            when {
                phase == ScrubPhase.Idle -> tracker.reset(focus, count)
                previousPhase == ScrubPhase.Idle && phase == ScrubPhase.Scrubbing -> {
                    tracker.reset(focus, count)
                    haptics.segmentTick()
                }
                else -> when (tracker.update(focus, count)) {
                    is ScrubHapticEvent.Tick -> haptics.segmentTick()
                    ScrubHapticEvent.Edge -> haptics.edge()
                    null -> Unit
                }
            }
            previousPhase = phase
        }
    }
}
