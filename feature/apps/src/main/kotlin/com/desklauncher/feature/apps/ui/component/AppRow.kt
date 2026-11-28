package com.desklauncher.feature.apps.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.component.pressScale
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSize
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.apps.R
import com.desklauncher.feature.apps.domain.model.InstalledApp

/** 아이콘을 비동기로 가져오는 함수 — Preview에서는 null */
typealias AppIconProvider = suspend (key: String, sizePx: Int) -> ImageBitmap?

/**
 * 앱 한 줄. 길게 누르면 고정 메뉴 (APPS-02).
 * @param isPinned 이미 하단 줄에 고정됐는지 — 메뉴 문구가 바뀐다
 */
@Composable
fun AppRow(
    app: InstalledApp,
    iconProvider: AppIconProvider?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onTogglePin: () -> Unit = {},
) {
    val interaction = remember { MutableInteractionSource() }
    var menuOpen by remember { mutableStateOf(false) }
    val menuLabel = stringResource(R.string.apps_menu)
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DeskSize.control + DeskSpacing.xs)
                .pressScale(interaction)
                .clip(DeskShape.control)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onLongClickLabel = menuLabel,
                    onLongClick = { menuOpen = true },
                    onClick = onClick,
                )
                .padding(horizontal = DeskSpacing.s, vertical = DeskSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DeskSpacing.s),
        ) {
            AppIcon(key = app.key, iconProvider = iconProvider)
            Text(
                text = app.label,
                style = DeskType.body,
                color = DeskTheme.colors.onPaper,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(if (isPinned) R.string.apps_unpin else R.string.apps_pin), style = DeskType.label) },
                onClick = {
                    menuOpen = false
                    onTogglePin()
                },
            )
        }
    }
}

@Composable
internal fun AppIcon(key: String, iconProvider: AppIconProvider?) {
    val sizePx = with(LocalDensity.current) { IconSize.roundToPx() }
    val icon by produceState<ImageBitmap?>(initialValue = null, key, iconProvider) {
        value = iconProvider?.invoke(key, sizePx)
    }
    Box(Modifier.size(IconSize).clip(DeskShape.control).background(DeskTheme.colors.desk)) {
        icon?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.size(IconSize)) }
    }
}

private val IconSize = 40.dp

@DeskFontScalePreviews
@Composable
private fun AppRowPreview() {
    DeskTheme {
        AppRow(
            app = InstalledApp("com.kakao.talk/.Main", "com.kakao.talk", "카카오톡"),
            iconProvider = null,
            onClick = {},
        )
    }
}
