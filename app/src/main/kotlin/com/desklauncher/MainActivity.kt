package com.desklauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.navigation.DeskNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 단일 액티비티 + 기본 런처(HOME) 진입점.
 * 홈 버튼 재진입 시 책상 복귀(onNewIntent)는 SHELL-06에서 구현한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var toolRegistry: ToolRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DeskTheme {
                DeskNavHost(toolRegistry = toolRegistry)
            }
        }
    }
}
