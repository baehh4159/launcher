package com.desklauncher.feature.diary.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.feature.diary.domain.model.DiaryEntry
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.domain.repository.DiaryRepository
import com.desklauncher.feature.diary.navigation.DiaryEditorDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/** 그 날의 일기 1건 편집 + 자동 저장 (DIARY-01). 프로젝트 메모 편집기와 같은 방식. */
@OptIn(FlowPreview::class)
@HiltViewModel
class DiaryEditorViewModel internal constructor(
    destination: DiaryEditorDestination,
    private val repository: DiaryRepository,
    private val appScope: CoroutineScope,
    private val clock: Clock,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        repository: DiaryRepository,
        @ApplicationScope appScope: CoroutineScope,
        clock: Clock,
    ) : this(savedStateHandle.toRoute<DiaryEditorDestination>(), repository, appScope, clock)

    val date: LocalDate = destination.epochDay?.let(LocalDate::ofEpochDay) ?: LocalDate.now(clock)
    private var original: DiaryEntry? = null
    private var isDeleted = false
    private val edits = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    var isLoading by mutableStateOf(true)
        private set
    var body by mutableStateOf("")
        private set
    var mood by mutableStateOf<Mood?>(null)
        private set
    var isSaved by mutableStateOf(true)
        private set
    val hasEntry: Boolean get() = original != null

    init {
        viewModelScope.launch {
            repository.get(date)?.let { entry ->
                original = entry
                body = entry.body
                mood = entry.mood
            }
            isLoading = false
        }
        viewModelScope.launch { edits.debounce(AutoSaveDelayMillis).collect { save() } }
    }

    fun onBodyChange(value: String) {
        body = value
        changed()
    }

    fun onMoodChange(value: Mood?) {
        mood = value
        changed()
    }

    fun delete(onDeleted: () -> Unit) {
        isDeleted = true
        viewModelScope.launch {
            repository.delete(date)
            onDeleted()
        }
    }

    private fun changed() {
        isSaved = false
        edits.tryEmit(Unit)
    }

    private suspend fun save() {
        if (isDeleted || isSaved) return
        if (original == null && body.isBlank() && mood == null) return
        val now = clock.instant()
        val entry = (original ?: DiaryEntry(id = UUID.randomUUID().toString(), date = date, createdAt = now, updatedAt = now))
            .copy(body = body, mood = mood)
        repository.save(entry)
        original = entry
        isSaved = true
    }

    override fun onCleared() {
        appScope.launch { save() }
    }

    internal companion object {
        const val AutoSaveDelayMillis = 500L
    }
}
