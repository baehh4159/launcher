package com.desklauncher.window

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.desklauncher.R
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType

/** M0 시제품에서 아직 없는 창 — 어떤 칸이 열렸는지와 언제 열리는지만 정직하게 알린다 */
@Composable
fun ComingSoonWindow(key: DeskWindowKey, modifier: Modifier = Modifier) {
    val (title, body) = when (key) {
        DeskWindowKey.SoonNotebook -> R.string.soon_notebook_title to R.string.soon_notebook_body
        DeskWindowKey.SoonAll -> R.string.soon_all_title to R.string.soon_notebook_body
        DeskWindowKey.SoonTools -> R.string.soon_tools_title to R.string.soon_tools_body
        DeskWindowKey.SoonQuickWrite -> R.string.soon_quick_write_title to R.string.soon_paper_body
        DeskWindowKey.SoonResume, DeskWindowKey.Apps, DeskWindowKey.Settings -> R.string.soon_resume_title to R.string.soon_paper_body
    }
    val colors = DeskTheme.colors
    Column(
        modifier = modifier.fillMaxSize().padding(DeskSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(DeskSpacing.s, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(title), style = DeskType.pageTitle, color = colors.onPaper)
        Text(stringResource(body), style = DeskType.body, color = colors.onPaper, textAlign = TextAlign.Center)
        Text(stringResource(R.string.soon_footer), style = DeskType.caption, color = colors.muted, textAlign = TextAlign.Center)
    }
}

@DeskPreviews
@Composable
private fun ComingSoonWindowPreview() {
    DeskTheme { ComingSoonWindow(DeskWindowKey.SoonNotebook) }
}
