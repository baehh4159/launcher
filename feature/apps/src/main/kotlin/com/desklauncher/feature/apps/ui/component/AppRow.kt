package com.desklauncher.feature.apps.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.apps.R
import com.desklauncher.feature.apps.domain.model.InstalledApp

/** 아이콘 불러오기 함수. Preview에서는 null을 돌려 첫 글자 자리표시를 보여 준다. */
typealias AppIconProvider = suspend (componentKey: String, sizePx: Int) -> ImageBitmap?

/** 길게 누르면 메뉴 (SHELL-08). */
@Composable
internal fun AppRow(
    app: InstalledApp,
    iconProvider: AppIconProvider,
    onClick: () -> Unit,
    onMenuAction: (AppMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuOpen = true },
                    onLongClickLabel = stringResource(R.string.apps_row_long_press),
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIcon(app = app, iconProvider = iconProvider)
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (app.isHidden) {
                Text(
                    text = stringResource(R.string.apps_hidden_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            AppMenuAction.entries
                .filter { it.isAvailable(app) }
                .forEach { action ->
                    DropdownMenuItem(
                        text = { Text(stringResource(action.label(app))) },
                        onClick = {
                            menuOpen = false
                            onMenuAction(action)
                        },
                    )
                }
        }
    }
}

internal enum class AppMenuAction {
    ToggleFavorite,
    ToggleHidden,
    AppInfo,
    Uninstall,
    ;

    fun isAvailable(app: InstalledApp): Boolean = this != Uninstall || !app.isSystemApp

    @StringRes
    fun label(app: InstalledApp): Int = when (this) {
        ToggleFavorite -> if (app.isFavorite) R.string.apps_menu_favorite_remove else R.string.apps_menu_favorite_add
        ToggleHidden -> if (app.isHidden) R.string.apps_menu_unhide else R.string.apps_menu_hide
        AppInfo -> R.string.apps_menu_info
        Uninstall -> R.string.apps_menu_uninstall
    }
}

@Composable
private fun AppIcon(app: InstalledApp, iconProvider: AppIconProvider) {
    val sizePx = with(LocalDensity.current) { IconSize.roundToPx() }
    val icon by produceState<ImageBitmap?>(initialValue = null, app.componentKey, sizePx) {
        value = iconProvider(app.componentKey, sizePx)
    }
    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(IconSize))
    } else {
        Box(
            modifier = Modifier
                .size(IconSize)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = app.label.take(1),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private val IconSize = 40.dp

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun AppRowPreview() {
    DeskTheme {
        AppRow(
            app = InstalledApp("com.kakao.talk/.Main", "com.kakao.talk", "카카오톡", isHidden = true),
            iconProvider = { _, _ -> null },
            onClick = {},
            onMenuAction = {},
        )
    }
}
