package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.text.asString
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.R
import com.desklauncher.feature.desk.ui.DeskTabUi
import com.desklauncher.feature.desk.ui.preview.DeskPreviewData

/**
 * M0 임시 인덱스 탭: 단순 세로 목록 + 탭하여 열기.
 * SHELL-03(스크럽·플링·스냅)에서 `IndexScrubStrip`으로 교체한다.
 */
@Composable
internal fun PlaceholderTabColumn(
    tabs: List<DeskTabUi>,
    onTabClick: (toolId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listDescription = stringResource(R.string.desk_tab_list)
    Column(
        modifier = modifier.semantics { contentDescription = listDescription },
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        tabs.forEach { tab ->
            val title = tab.title.asString()
            val openLabel = stringResource(R.string.desk_tab_open, title)
            Surface(
                onClick = { onTabClick(tab.toolId) },
                color = tab.color.container,
                contentColor = tab.color.onContainer,
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.semantics { contentDescription = openLabel },
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                    Icon(imageVector = tab.icon, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@DeskPreviews
@Composable
private fun PlaceholderTabColumnPreview() {
    DeskTheme {
        PlaceholderTabColumn(tabs = DeskPreviewData.withTabs.tabs, onTabClick = {})
    }
}
