package com.desklauncher.feature.apps.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.motion.LocalReducedMotion
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.apps.R
import kotlin.math.roundToInt

/**
 * 초성 색인 (APPS-01). 보이는 글자 위를 누르거나 훑으면 그 묶음으로 이동한다 — 보이는 곳 = 누르는 곳.
 * 현재 글자 표시는 Glide 스프링으로 미끄러지고(Beautiful UI Sidebar Nav), 글자가 바뀔 때 탐색 진동.
 */
@Composable
fun LetterIndex(
    keys: List<String>,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (keys.isEmpty()) return
    val haptics = rememberDeskHaptics()
    val currentOnSelect by rememberUpdatedState(onSelect)
    var selected by remember(keys) { mutableStateOf<Int?>(null) }
    val description = stringResource(R.string.apps_index_description)
    val reduced = LocalReducedMotion.current

    BoxWithConstraints(modifier = modifier.width(IndexWidth).fillMaxHeight().semantics { contentDescription = description }) {
        val itemHeight = min(maxHeight / keys.size, MaxItemHeight)
        val itemPx = with(LocalDensity.current) { itemHeight.toPx() }
        val markerY by animateFloatAsState(
            targetValue = (selected ?: 0) * itemPx,
            animationSpec = if (reduced) snap() else DeskMotion.glide(),
            label = "indexMarker",
        )
        if (selected != null) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, (markerY + (itemPx - MarkerSizePx(itemPx)) / 2f).roundToInt()) }
                    .size(with(LocalDensity.current) { MarkerSizePx(itemPx).toDp() })
                    .background(DeskTheme.colors.ink, CircleShape),
            )
        }
        Column(
            modifier = Modifier
                .height(itemHeight * keys.size)
                .pointerInput(keys, itemPx) {
                    fun pick(y: Float) {
                        val index = (y / itemPx).toInt().coerceIn(0, keys.lastIndex)
                        if (index != selected) {
                            selected = index
                            haptics.tick()
                            currentOnSelect(index)
                        }
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        pick(down.position.y)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            pick(change.position.y)
                        }
                        selected = null
                    }
                },
        ) {
            keys.forEachIndexed { index, key ->
                Text(
                    text = key,
                    style = DeskType.caption,
                    color = if (index == selected) DeskTheme.colors.onInk else DeskTheme.colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(IndexWidth).height(itemHeight),
                )
            }
        }
    }
}

private val IndexWidth = 28.dp
private val MaxItemHeight = 22.dp
private fun MarkerSizePx(itemPx: Float): Float = itemPx * 1.1f

@DeskFontScalePreviews
@Composable
private fun LetterIndexPreview() {
    DeskTheme {
        Box(Modifier.height(320.dp)) {
            LetterIndex(keys = listOf("ㄱ", "ㄴ", "ㅁ", "ㅅ", "ㅇ", "ㅋ", "A", "G", "#"), onSelect = {})
        }
    }
}
