package com.desklauncher.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.GripPosition
import com.desklauncher.core.datastore.HapticPreference
import com.desklauncher.core.datastore.ThemePreference
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.settings.R
import com.desklauncher.feature.settings.role.rememberHomeRoleState
import com.desklauncher.feature.settings.ui.component.ChoiceSettingRow
import com.desklauncher.feature.settings.ui.component.SettingsSectionHeader
import com.desklauncher.feature.settings.ui.component.SwitchSettingRow

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }
    val homeRole = rememberHomeRoleState()
    SettingsScreen(
        state = uiState,
        appVersion = version,
        isDefaultLauncher = homeRole.isDefault,
        onRequestDefaultLauncher = homeRole.request,
        onOpenHomeSettings = homeRole.openSystemSettings,
        onAction = { action -> if (action == SettingsAction.Back) onBack() else viewModel.onAction(action) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    appVersion: String,
    isDefaultLauncher: Boolean,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
    onRequestDefaultLauncher: () -> Unit = {},
    onOpenHomeSettings: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(SettingsAction.Back) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                SettingsContent(
                    settings = state.settings,
                    appVersion = appVersion,
                    onAction = onAction,
                    launcherSection = {
                        DefaultLauncherSection(isDefaultLauncher, onRequestDefaultLauncher, onOpenHomeSettings)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    settings: DeskSettings,
    appVersion: String,
    onAction: (SettingsAction) -> Unit,
    launcherSection: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        launcherSection()
        SettingsSectionHeader(stringResource(R.string.settings_section_desk))
        ChoiceSettingRow(
            title = stringResource(R.string.settings_haptic),
            options = listOf(
                HapticPreference.Off to stringResource(R.string.settings_haptic_off),
                HapticPreference.Light to stringResource(R.string.settings_haptic_light),
                HapticPreference.Default to stringResource(R.string.settings_haptic_default),
            ),
            selected = settings.haptic,
            onSelect = { onAction(SettingsAction.SetHaptic(it)) },
        )
        ChoiceSettingRow(
            title = stringResource(R.string.settings_grip),
            description = stringResource(R.string.settings_grip_desc),
            options = listOf(
                GripPosition.Top to stringResource(R.string.settings_grip_top),
                GripPosition.Middle to stringResource(R.string.settings_grip_middle),
                GripPosition.Bottom to stringResource(R.string.settings_grip_bottom),
            ),
            selected = settings.gripPosition,
            onSelect = { onAction(SettingsAction.SetGrip(it)) },
        )
        SwitchSettingRow(
            title = stringResource(R.string.settings_drawer_autofocus),
            description = stringResource(R.string.settings_drawer_autofocus_desc),
            checked = settings.appDrawerAutoFocusSearch,
            onCheckedChange = { onAction(SettingsAction.SetAutoFocusSearch(it)) },
        )

        SettingsSectionHeader(stringResource(R.string.settings_section_display))
        ChoiceSettingRow(
            title = stringResource(R.string.settings_theme),
            options = listOf(
                ThemePreference.System to stringResource(R.string.settings_theme_system),
                ThemePreference.Light to stringResource(R.string.settings_theme_light),
                ThemePreference.Dark to stringResource(R.string.settings_theme_dark),
            ),
            selected = settings.theme,
            onSelect = { onAction(SettingsAction.SetTheme(it)) },
        )

        SettingsSectionHeader(stringResource(R.string.settings_section_about))
        Text(
            text = stringResource(R.string.settings_version, appVersion),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Text(
            text = stringResource(R.string.settings_backup_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DefaultLauncherSection(
    isDefault: Boolean,
    onRequest: () -> Unit,
    onOpenSystemSettings: () -> Unit,
) {
    SettingsSectionHeader(stringResource(R.string.settings_section_launcher))
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(if (isDefault) R.string.settings_launcher_is_default else R.string.settings_launcher_not_default),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (!isDefault) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRequest) { Text(stringResource(R.string.settings_launcher_request)) }
                TextButton(onClick = onOpenSystemSettings) { Text(stringResource(R.string.settings_launcher_system)) }
            }
        }
    }
}

@DeskPreviews
@Composable
private fun SettingsScreenPreview() {
    DeskTheme {
        SettingsScreen(state = SettingsUiState(isLoading = false), appVersion = "0.1.0", isDefaultLauncher = false, onAction = {})
    }
}

@DeskPreviews
@Composable
private fun SettingsScreenDefaultLauncherPreview() {
    DeskTheme {
        SettingsScreen(state = SettingsUiState(isLoading = false), appVersion = "0.1.0", isDefaultLauncher = true, onAction = {})
    }
}

@DeskPreviews
@Composable
private fun SettingsScreenLoadingPreview() {
    DeskTheme {
        SettingsScreen(state = SettingsUiState(isLoading = true), appVersion = "0.1.0", isDefaultLauncher = false, onAction = {})
    }
}
