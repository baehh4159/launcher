package com.desklauncher.feature.doodle.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoodleStateTest {

    private fun stroke(id: Float) = Stroke(argb = 0, widthPx = 4f, points = listOf(id, id))

    // 빈 획은 추가하지 않는다
    @Test
    fun add_ignoresEmptyStroke() {
        assertTrue(DoodleState().add(Stroke(0, 4f)).isEmpty)
        assertFalse(DoodleState().add(Stroke(0, 4f)).canUndo)
    }

    // 실행 취소·다시 실행
    @Test
    fun undoRedo_walkHistory() {
        val drawn = DoodleState().add(stroke(1f)).add(stroke(2f))
        val undone = drawn.undo()
        assertEquals(1, undone.strokes.size)
        assertTrue(undone.canRedo)
        assertEquals(2, undone.redo().strokes.size)
        assertTrue(drawn.undo().undo().isEmpty)
        assertFalse(drawn.undo().undo().canUndo)
    }

    // 새로 그리면 다시 실행 목록은 사라진다
    @Test
    fun add_afterUndo_clearsRedo() {
        val state = DoodleState().add(stroke(1f)).add(stroke(2f)).undo().add(stroke(3f))
        assertFalse(state.canRedo)
        assertEquals(listOf(1f, 3f), state.strokes.map { it.points.first() })
    }

    // 전체 지우기도 되돌릴 수 있다
    @Test
    fun clear_isUndoable() {
        val cleared = DoodleState().add(stroke(1f)).add(stroke(2f)).clear()
        assertTrue(cleared.isEmpty)
        assertEquals(2, cleared.undo().strokes.size)
    }
}
