package com.desklauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.desklauncher.core.designsystem.theme.DeskTheme
import dagger.hilt.android.AndroidEntryPoint

/** 단일 액티비티 + 기본 홈(HOME) 진입점. 책상과 창은 기능 ID별로 조립한다. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeskTheme {
                Box(Modifier.fillMaxSize().background(DeskTheme.colors.desk))
            }
        }
    }
}
