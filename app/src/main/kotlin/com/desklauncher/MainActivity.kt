package com.desklauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.navigation.DeskNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/** 단일 액티비티 + 기본 런처(HOME) 진입점. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var toolRegistry: ToolRegistry

    // singleTask라 홈 버튼은 onNewIntent로 들어온다 (SHELL-06).
    private val homePresses = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DeskTheme {
                DeskNavHost(toolRegistry = toolRegistry, homePresses = homePresses.asSharedFlow())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.isHomeIntent()) homePresses.tryEmit(Unit)
    }

    private fun Intent.isHomeIntent(): Boolean =
        action == Intent.ACTION_MAIN && hasCategory(Intent.CATEGORY_HOME)
}
