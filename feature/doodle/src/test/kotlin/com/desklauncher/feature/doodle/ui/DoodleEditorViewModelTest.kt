package com.desklauncher.feature.doodle.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.feature.doodle.domain.Stroke
import com.desklauncher.feature.doodle.domain.model.Doodle
import com.desklauncher.feature.doodle.domain.model.DoodleFile
import com.desklauncher.feature.doodle.domain.repository.DoodleRepository
import com.desklauncher.feature.doodle.navigation.DoodleEditorDestination
import com.desklauncher.feature.doodle.ui.editor.DoodleEditorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DoodleEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeDoodleRepository()
    private fun stroke() = Stroke(0, 4f, points = listOf(1f, 1f, 2f, 2f))

    // 그리고 저장하면 캔버스 크기와 함께 저장, 다시 저장하면 같은 낙서를 덮어쓴다
    @Test
    fun save_persistsWithCanvasSize() = runTest {
        val viewModel = DoodleEditorViewModel(DoodleEditorDestination(), repository, backgroundScope)
        viewModel.onCanvasSize(1080, 1800)
        viewModel.addStroke(stroke())
        assertTrue(viewModel.isDirty)

        viewModel.save()
        assertFalse(viewModel.isDirty)
        val saved = repository.files.values.single()
        assertEquals(1080, saved.width)
        assertEquals(1, saved.strokes.size)

        viewModel.addStroke(stroke())
        viewModel.save()
        assertEquals(1, repository.files.size)
        assertEquals(2, repository.files.values.single().strokes.size)
    }

    // 빈 새 낙서는 만들지 않는다
    @Test
    fun emptyNewDoodle_isNotSaved() = runTest {
        val viewModel = DoodleEditorViewModel(DoodleEditorDestination(), repository, backgroundScope)
        viewModel.onCanvasSize(100, 100)
        viewModel.addStroke(stroke())
        viewModel.clear()
        viewModel.save()
        assertTrue(repository.files.isEmpty())
    }

    // 불러온 낙서는 현재 캔버스 크기에 맞춘다
    @Test
    fun load_fitsToCanvas() = runTest {
        repository.files["n1"] = DoodleFile(width = 200, height = 200, strokes = listOf(Stroke(0, 10f, points = listOf(100f, 100f))))
        val viewModel = DoodleEditorViewModel(DoodleEditorDestination("n1"), repository, backgroundScope)
        viewModel.onCanvasSize(100, 300)
        assertEquals(listOf(50f, 50f), viewModel.doodle.strokes.single().points)
        assertFalse(viewModel.isDirty)
    }
}

internal class FakeDoodleRepository : DoodleRepository {
    val files = linkedMapOf<String, DoodleFile>()
    private var next = 1
    override val doodles = MutableStateFlow<List<Doodle>>(emptyList())
    override suspend fun load(id: String) = files[id]
    override suspend fun save(id: String?, file: DoodleFile): String {
        val key = id ?: "n${next++}"
        files[key] = file
        return key
    }
    override suspend fun delete(id: String) {
        files.remove(id)
    }
}
