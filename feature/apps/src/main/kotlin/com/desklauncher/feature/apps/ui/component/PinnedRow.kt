package com.desklauncher.feature.apps.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.desklauncher.core.designsystem.component.pressScale
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.apps.R
import com.desklauncher.feature.apps.domain.model.InstalledApp

/**
 * 앱 서랍 하단 고정 앱 줄 (APPS-02, PRD §12). 서랍을 연 직후 키보드 없이 누를 수 있어
 * '앱 → 카카오톡'이 2탭이 된다. 길게 누르면 앞으로·뒤로·고정 해제.
 */
@Composable
fun PinnedRow(
    apps: List<InstalledApp>,
    iconProvider: AppIconProvider?,
    onLaunch: (InstalledApp) -> Unit,
    onMove: (InstalledApp, delta: Int) -> Unit,
    onUnpin: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty()) return
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = DeskSpacing.xs),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        apps.forEachIndexed { index, app ->
            PinnedItem(
                app = app,
                iconProvider = iconProvider,
                canMoveEarlier = index > 0,
                canMoveLater = index < apps.lastIndex,
                onLaunch = { onLaunch(app) },
                onMove = { delta -> onMove(app, delta) },
                onUnpin = { onUnpin(app) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PinnedItem(
    app: InstalledApp,
    iconProvider: AppIconProvider?,
    canMoveEarlier: Boolean,
    canMoveLater: Boolean,
    onLaunch: () -> Unit,
    onMove: (Int) -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    var menuOpen by remember { mutableStateOf(false) }
    val menuLabel = stringResource(R.string.apps_menu)
    Box(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(interaction)
                .clip(DeskShape.control)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onLongClickLabel = menuLabel,
                    onLongClick = { menuOpen = true },
                    onClick = onLaunch,
                )
                .padding(vertical = DeskSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIcon(key = app.key, iconProvider = iconProvider)
            Text(
                text = app.label,
                style = DeskType.caption,
                color = DeskTheme.colors.onPaper,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = DeskSpacing.xxs),
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            if (canMoveEarlier) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.apps_move_earlier), style = DeskType.label) },
                    onClick = { menuOpen = false; onMove(-1) },
                )
            }
            if (canMoveLater) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.apps_move_later), style = DeskType.label) },
                    onClick = { menuOpen = false; onMove(1) },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.apps_unpin), style = DeskType.label) },
                onClick = { menuOpen = false; onUnpin() },
            )
        }
    }
}

@DeskFontScalePreviews
@Composable
private fun PinnedRowPreview() {
    DeskTheme {
        PinnedRow(
            apps = listOf("카카오톡", "카메라", "토스", "캘린더", "Chrome").mapIndexed { i, label -> InstalledApp("p$i/.M", "p$i", label) },
            iconProvider = null,
            onLaunch = {},
            onMove = { _, _ -> },
            onUnpin = {},
        )
    }
}
