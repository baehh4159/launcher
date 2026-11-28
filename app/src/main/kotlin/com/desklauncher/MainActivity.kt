package com.desklauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.ui.DeskRoute
import dagger.hilt.android.AndroidEntryPoint

/** 단일 액티비티 + 기본 홈(HOME) 진입점. 책상 위에 어떤 창을 띄울지는 여기서 조립한다 (AGENTS.md §4.5). */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeskTheme {
                // 창(공책·앱 서랍·문구)은 APPS-01·M1 이후 기능 ID에서 연결한다.
                DeskRoute(onOpenSlot = {}, onQuickWrite = {}, onResume = {})
            }
        }
    }
}
