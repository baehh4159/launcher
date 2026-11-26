package com.desklauncher.debugtools

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Brush

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.desklauncher.R
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import kotlinx.serialization.Serializable

/**
 * debug 빌드 전용 샘플 도구 (M1 서류철 스트립 실기기 검증용).
 * id는 실제 도구와 겹치지 않게 `debug_` 접두어를 쓴다. 실제 도구가 모두 생기면(M5) 삭제한다.
 */
internal class SampleTool(
    key: String,
    @param:StringRes override val titleRes: Int,
    override val icon: ImageVector,
    override val defaultTabColor: DeskTabColor,
    override val defaultOrder: Int,
) : DeskTool {
    override val id: String = "debug_$key"
    override val descriptionRes: Int = R.string.sample_tool_description
    override val defaultEnabled: Boolean = true
    override val startDestination: Any = SampleToolDestination(id)

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        // 모든 샘플이 같은 라우트 타입을 쓰므로 목록의 첫 샘플만 등록한다.
        if (SampleTools.all.firstOrNull() !== this@SampleTool) return
        composable<SampleToolDestination> { entry ->
            val toolId = entry.toRoute<SampleToolDestination>().toolId
            val title = SampleTools.all.firstOrNull { it.id == toolId }?.titleRes ?: R.string.sample_tool_doodle
            SampleToolScreen(titleRes = title)
        }
    }
}

@Serializable
internal data class SampleToolDestination(val toolId: String)

internal object SampleTools {
    val all: List<SampleTool> = listOf(
        SampleTool("doodle", R.string.sample_tool_doodle, Icons.Outlined.Brush, DeskTabColor.Purple, 6),
        SampleTool("alarm", R.string.sample_tool_alarm, Icons.Outlined.Alarm, DeskTabColor.Indigo, 7),
        SampleTool("files", R.string.sample_tool_files, Icons.AutoMirrored.Outlined.InsertDriveFile, DeskTabColor.Gray, 8),
    )
}
