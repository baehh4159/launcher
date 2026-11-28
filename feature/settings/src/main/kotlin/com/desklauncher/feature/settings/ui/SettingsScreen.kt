package com.desklauncher.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.HapticSetting
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.settings.R
import com.desklauncher.feature.settings.ui.component.ChoiceRow

@Composable
fun SettingsRoute(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

/** 설정 창 (창 안에서만 스크롤 — 책상은 스크롤하지 않는다, PRD §3.2) */
@Composable
fun SettingsScreen(state: SettingsUiState, onAction: (SettingsAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DeskSpacing.m),
    ) {
        Text(stringResource(R.string.settings_title), style = DeskType.pageTitle, color = colors.onPaper)
        Spacer(Modifier.height(DeskSpacing.l))
        Text(stringResource(R.string.settings_haptics_section), style = DeskType.caption, color = colors.muted)
        Spacer(Modifier.height(DeskSpacing.xs))
        HapticSetting.entries.forEach { option ->
            val (title, description) = when (option) {
                HapticSetting.Full -> R.string.settings_haptics_full to R.string.settings_haptics_full_desc
                HapticSetting.ConfirmOnly -> R.string.settings_haptics_confirm to R.string.settings_haptics_confirm_desc
                HapticSetting.Off -> R.string.settings_haptics_off to R.string.settings_haptics_off_desc
            }
            ChoiceRow(
                title = stringResource(title),
                description = stringResource(description),
                selected = state.settings.haptics == option,
                onClick = { onAction(SettingsAction.SetHaptics(option)) },
            )
        }
    }
}

@DeskPreviews
@Composable
private fun SettingsScreenPreview() {
    DeskTheme { SettingsScreen(state = SettingsUiState(DeskSettings()), onAction = {}) }
}

@DeskPreviews
@Composable
private fun SettingsScreenOffPreview() {
    DeskTheme { SettingsScreen(state = SettingsUiState(DeskSettings(haptics = HapticSetting.Off)), onAction = {}) }
}
