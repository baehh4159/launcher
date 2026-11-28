package com.desklauncher

import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.HapticSetting
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.core.designsystem.component.DeskWindowHost
import com.desklauncher.core.designsystem.haptics.HapticMode
import com.desklauncher.core.designsystem.haptics.LocalHapticMode
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.apps.ui.AppsRoute
import com.desklauncher.feature.desk.strip.SlotKind
import com.desklauncher.feature.desk.ui.DeskRoute
import com.desklauncher.feature.settings.ui.SettingsRoute
import com.desklauncher.window.ComingSoonWindow
import com.desklauncher.window.DeskWindowKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

/** 단일 액티비티 + 기본 홈(HOME) 진입점. 책상 위에 어떤 창을 띄울지는 여기서 조립한다 (AGENTS.md §4.5). */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    // singleTask라 이미 홈에 있을 때 홈 버튼은 onNewIntent로 들어온다 (HOME-01)
    private val homePresses = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) homePresses.tryEmit(Unit)
    }

    private fun holdsHomeRole(): Boolean = getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = DeskSettings())
            CompositionLocalProvider(LocalHapticMode provides settings.haptics.toHapticMode()) {
                DeskTheme {
                    // 열린 창 (회전·프로세스 복원 시 유지). M0는 창 하나.
                    var window by rememberSaveable { mutableStateOf<DeskWindowKey?>(null) }
                    // 홈 버튼: 어떤 창이 열려 있든 책상으로 (PRD §1.1)
                    LaunchedEffect(Unit) { homePresses.collect { window = null } }
                    var isDefaultHome by remember { mutableStateOf(holdsHomeRole()) }
                    LifecycleResumeEffect(Unit) {
                        isDefaultHome = holdsHomeRole()
                        onPauseOrDispose { }
                    }
                    val roleRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        isDefaultHome = holdsHomeRole()
                    }
                    Box(Modifier.fillMaxSize()) {
                        DeskRoute(
                            onOpenSlot = { slot ->
                                window = when (slot.kind) {
                                    SlotKind.Apps -> DeskWindowKey.Apps
                                    SlotKind.Notebook -> DeskWindowKey.SoonNotebook
                                    SlotKind.All -> DeskWindowKey.SoonAll
                                    SlotKind.Tools -> DeskWindowKey.SoonTools
                                }
                            },
                            onQuickWrite = { window = DeskWindowKey.SoonQuickWrite },
                            onResume = { window = DeskWindowKey.SoonResume },
                            onOpenSettings = { window = DeskWindowKey.Settings },
                            onSetDefaultHome = {
                                roleRequest.launch(getSystemService(RoleManager::class.java).createRequestRoleIntent(RoleManager.ROLE_HOME))
                            },
                            scrubEnabled = window == null,
                            isDefaultHome = isDefaultHome,
                        )
                        DeskWindowHost(window = window, onDismiss = { window = null }) { key ->
                            when (key) {
                                DeskWindowKey.Apps -> AppsRoute(onLaunched = { window = null })
                                DeskWindowKey.Settings -> SettingsRoute()
                                else -> ComingSoonWindow(key)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun HapticSetting.toHapticMode(): HapticMode = when (this) {
    HapticSetting.Full -> HapticMode.Full
    HapticSetting.ConfirmOnly -> HapticMode.ConfirmOnly
    HapticSetting.Off -> HapticMode.Off
}
