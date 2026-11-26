package com.desklauncher.feature.apps.di

import com.desklauncher.feature.apps.ui.icon.AppIconLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** 아이콘 로더는 화면 전용이라 ViewModel(테스트 대상)에 넣지 않고 Route에서 꺼내 쓴다. */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AppIconEntryPoint {
    fun appIconLoader(): AppIconLoader
}
