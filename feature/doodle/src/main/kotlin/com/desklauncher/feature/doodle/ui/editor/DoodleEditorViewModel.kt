package com.desklauncher.feature.doodle.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.desklauncher.core.designsystem.theme.DeskPenColor
import com.desklauncher.feature.doodle.domain.DoodleState
import com.desklauncher.feature.doodle.domain.PenWidth
import com.desklauncher.feature.doodle.domain.Stroke
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** 낙서 편집 상태 (DOODLE-01). 그림은 Compose 상태로 들고 있어 그리는 동안 끊김이 없다. */
@HiltViewModel
class DoodleEditorViewModel @Inject constructor() : ViewModel() {

    var doodle by mutableStateOf(DoodleState())
        private set
    var color by mutableStateOf(DeskPenColor.Ink)
        private set
    var width by mutableStateOf(PenWidth.Medium)
        private set
    var eraser by mutableStateOf(false)
        private set

    fun addStroke(stroke: Stroke) {
        doodle = doodle.add(stroke)
    }

    fun undo() {
        doodle = doodle.undo()
    }

    fun redo() {
        doodle = doodle.redo()
    }

    fun clear() {
        doodle = doodle.clear()
    }

    fun selectColor(value: DeskPenColor) {
        color = value
        eraser = false
    }

    fun selectWidth(value: PenWidth) {
        width = value
    }

    fun selectEraser(value: Boolean) {
        eraser = value
    }
}
