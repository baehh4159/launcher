package com.desklauncher.feature.settings.role

import android.app.role.RoleManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * 기본 홈 앱(ROLE_HOME) 상태와 요청 (SHELL-01).
 * 사용자가 여러 번 거절하면 시스템이 대화상자를 더 띄우지 않으므로, 시스템 설정으로 가는 길도 함께 둔다.
 */
@Stable
class HomeRoleState internal constructor(
    isDefaultProvider: () -> Boolean,
    val request: () -> Unit,
    val openSystemSettings: () -> Unit,
) {
    private val isDefaultGetter = isDefaultProvider
    val isDefault: Boolean get() = isDefaultGetter()
}

@Composable
fun rememberHomeRoleState(): HomeRoleState {
    val context = LocalContext.current
    val roleManager = remember(context) { context.getSystemService(RoleManager::class.java) }
    var held by remember { mutableStateOf(roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true) }

    LifecycleResumeEffect(roleManager) {
        held = roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        held = roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
    }
    return remember(roleManager, launcher) {
        val openSettings = {
            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        HomeRoleState(
            isDefaultProvider = { held },
            request = {
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    launcher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                } else {
                    openSettings()
                }
            },
            openSystemSettings = openSettings,
        )
    }
}
