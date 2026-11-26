package com.desklauncher.feature.store.ui.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/**
 * LazyColumn 손잡이 드래그 정렬 (외부 라이브러리 없이).
 * 끌고 있는 항목의 중심이 다른 항목 위로 가면 [onMove]로 자리를 바꾸고,
 * 화면상 위치가 튀지 않게 [dragOffset]을 보정한다.
 */
@Stable
internal class ReorderState(
    private val listState: LazyListState,
    private val onMoveProvider: () -> (fromKey: Any, toKey: Any) -> Unit,
) {
    var draggingKey: Any? by mutableStateOf(null)
        private set
    var dragOffset: Float by mutableFloatStateOf(0f)
        private set

    fun start(key: Any) {
        draggingKey = key
        dragOffset = 0f
    }

    fun drag(deltaY: Float) {
        val key = draggingKey ?: return
        dragOffset += deltaY
        val visible = listState.layoutInfo.visibleItemsInfo
        val current = visible.firstOrNull { it.key == key } ?: return
        val center = current.offset + dragOffset + current.size / 2f
        val target = visible.firstOrNull { item ->
            item.key != key && item.contentType == ReorderableContentType &&
                center >= item.offset && center <= item.offset + item.size
        } ?: return
        onMoveProvider()(key, target.key)
        dragOffset += (current.offset - target.offset)
    }

    fun end() {
        draggingKey = null
        dragOffset = 0f
    }
}

/** 정렬 대상 항목에 지정하는 contentType. 머리말 등은 다른 값을 쓴다. */
internal const val ReorderableContentType = "reorderable"

@Composable
internal fun rememberReorderState(
    listState: LazyListState,
    onMove: (fromKey: Any, toKey: Any) -> Unit,
): ReorderState {
    val currentOnMove by rememberUpdatedState(onMove)
    return remember(listState) { ReorderState(listState) { currentOnMove } }
}
