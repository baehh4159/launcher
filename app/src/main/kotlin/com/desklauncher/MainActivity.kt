package com.desklauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.desklauncher.core.designsystem.component.DeskWindowHost
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.apps.ui.AppsRoute
import com.desklauncher.feature.desk.strip.SlotKind
import com.desklauncher.feature.desk.ui.DeskRoute
import com.desklauncher.window.ComingSoonWindow
import com.desklauncher.window.DeskWindowKey
import dagger.hilt.android.AndroidEntryPoint

/** 단일 액티비티 + 기본 홈(HOME) 진입점. 책상 위에 어떤 창을 띄울지는 여기서 조립한다 (AGENTS.md §4.5). */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeskTheme {
                // 열린 창 (회전·프로세스 복원 시 유지). M0는 창 하나.
                var window by rememberSaveable { mutableStateOf<DeskWindowKey?>(null) }
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
                        scrubEnabled = window == null,
                    )
                    DeskWindowHost(window = window, onDismiss = { window = null }) { key ->
                        when (key) {
                            DeskWindowKey.Apps -> AppsRoute(onLaunched = { window = null })
                            else -> ComingSoonWindow(key)
                        }
                    }
                }
            }
        }
    }
}
