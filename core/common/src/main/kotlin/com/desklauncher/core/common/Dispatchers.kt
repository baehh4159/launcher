package com.desklauncher.core.common

import javax.inject.Qualifier

/** I/O 작업용 Dispatcher (AGENTS.md §5 — Dispatcher는 주입받는다). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** CPU 계산용 Dispatcher. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
