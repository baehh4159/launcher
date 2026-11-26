package com.desklauncher.feature.doodle.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.core.designsystem.theme.DeskPenColor
import com.desklauncher.feature.doodle.domain.DoodleState
import com.desklauncher.feature.doodle.domain.PenWidth
import com.desklauncher.feature.doodle.domain.Stroke
import com.desklauncher.feature.doodle.domain.StrokeScaling
import com.desklauncher.feature.doodle.domain.model.DoodleFile
import com.desklauncher.feature.doodle.domain.repository.DoodleRepository
import com.desklauncher.feature.doodle.navigation.DoodleEditorDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 낙서 편집 (DOODLE-01~03). 그림은 Compose 상태로 들고 있어 그리는 동안 끊김이 없다.
 * 저장 버튼으로 저장하고, 저장하지 않은 변경은 화면을 떠날 때 앱 범위 코루틴으로 마저 저장한다.
 */
@HiltViewModel
class DoodleEditorViewModel internal constructor(
    destination: DoodleEditorDestination,
    private val repository: DoodleRepository,
    private val appScope: CoroutineScope,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        repository: DoodleRepository,
        @ApplicationScope appScope: CoroutineScope,
    ) : this(savedStateHandle.toRoute<DoodleEditorDestination>(), repository, appScope)

    private var doodleId: String? = destination.doodleId
    private var loaded: DoodleFile? = null
    private var canvasWidth = 0
    private var canvasHeight = 0

    var isLoading by mutableStateOf(destination.doodleId != null)
        private set
    var doodle by mutableStateOf(DoodleState())
        private set
    var color by mutableStateOf(DeskPenColor.Ink)
        private set
    var width by mutableStateOf(PenWidth.Medium)
        private set
    var eraser by mutableStateOf(false)
        private set
    var isDirty by mutableStateOf(false)
        private set
    var savedTick by mutableIntStateOf(0)
        private set

    init {
        destination.doodleId?.let { id ->
            viewModelScope.launch {
                loaded = repository.load(id)
                applyLoadedIfReady()
                isLoading = false
            }
        }
    }

    /** 캔버스 크기를 알게 되면(또는 바뀌면) 불러온 그림을 그 크기에 맞춘다 */
    fun onCanvasSize(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        applyLoadedIfReady()
    }

    private fun applyLoadedIfReady() {
        val file = loaded ?: return
        if (canvasWidth == 0 || canvasHeight == 0) return
        doodle = DoodleState(StrokeScaling.fit(file.strokes, file.width, file.height, canvasWidth, canvasHeight))
        loaded = null
    }

    fun addStroke(stroke: Stroke) = edit { doodle.add(stroke) }
    fun undo() = edit { doodle.undo() }
    fun redo() = edit { doodle.redo() }
    fun clear() = edit { doodle.clear() }

    private inline fun edit(block: () -> DoodleState) {
        val next = block()
        if (next != doodle) {
            doodle = next
            isDirty = true
        }
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

    fun save() {
        viewModelScope.launch {
            if (persist()) savedTick++
        }
    }

    /** 저장할 게 없으면 false. 새 낙서인데 비어 있으면 만들지 않는다. */
    private suspend fun persist(): Boolean {
        if (!isDirty || canvasWidth == 0) return false
        if (doodleId == null && doodle.isEmpty) return false
        val snapshot = DoodleFile(width = canvasWidth, height = canvasHeight, strokes = doodle.strokes)
        doodleId = repository.save(doodleId, snapshot)
        isDirty = false
        return true
    }

    override fun onCleared() {
        if (isDirty) appScope.launch { persist() }
    }
}
