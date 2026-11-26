package com.desklauncher.feature.memo.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.domain.repository.MemoRepository
import com.desklauncher.feature.memo.navigation.MemoEditorDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * 메모 편집 + 자동 저장 (MEMO-02).
 * 입력값은 Compose 상태로 들고 있어 타이핑이 끊기지 않게 하고, 저장은 멈춘 뒤 [AutoSaveDelayMillis] 후 한 번.
 * 화면을 떠날 때 남은 변경은 앱 범위 코루틴으로 마저 저장한다.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class MemoEditorViewModel internal constructor(
    destination: MemoEditorDestination,
    private val repository: MemoRepository,
    private val appScope: CoroutineScope,
    private val clock: Clock,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        repository: MemoRepository,
        @ApplicationScope appScope: CoroutineScope,
        clock: Clock,
    ) : this(savedStateHandle.toRoute<MemoEditorDestination>(), repository, appScope, clock)

    private val projectId = destination.projectId
    private val memoId = destination.memoId ?: UUID.randomUUID().toString()
    private var original: Memo? = null
    private var isDeleted = false
    private val edits = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    var isLoading by mutableStateOf(destination.memoId != null)
        private set
    var title by mutableStateOf("")
        private set
    var body by mutableStateOf("")
        private set
    var pinned by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(true)
        private set

    init {
        if (destination.memoId != null) {
            viewModelScope.launch {
                repository.getMemo(memoId)?.let { memo ->
                    original = memo
                    title = memo.title
                    body = memo.body
                    pinned = memo.pinned
                }
                isLoading = false
            }
        }
        viewModelScope.launch {
            edits.debounce(AutoSaveDelayMillis).collect { save() }
        }
    }

    fun onTitleChange(value: String) {
        title = value
        changed()
    }

    fun onBodyChange(value: String) {
        body = value
        changed()
    }

    fun togglePin() {
        pinned = !pinned
        changed()
    }

    fun delete(onDeleted: () -> Unit) {
        isDeleted = true
        viewModelScope.launch {
            repository.deleteMemo(memoId)
            onDeleted()
        }
    }

    private fun changed() {
        isSaved = false
        edits.tryEmit(Unit)
    }

    private suspend fun save() {
        if (isDeleted || isSaved) return
        // 새 메모를 열었다가 아무것도 적지 않으면 만들지 않는다
        if (original == null && title.isBlank() && body.isBlank()) return
        val base = original ?: Memo(id = memoId, projectId = projectId, createdAt = clock.instant(), updatedAt = clock.instant())
        val memo = base.copy(title = title, body = body, pinned = pinned)
        repository.saveMemo(memo)
        original = memo
        isSaved = true
    }

    override fun onCleared() {
        appScope.launch { save() }
    }

    internal companion object {
        const val AutoSaveDelayMillis = 500L
    }
}
