package com.desklauncher.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** 현재 시각은 Clock을 주입받아 쓴다 — 테스트에서 고정 시각을 넣기 위함. 매번 새로 만들어 시간대 변경을 따른다. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
