package com.desklauncher.core.common.dispatcher

import javax.inject.Qualifier

/** 주입할 코루틴 Dispatcher 종류. `Dispatchers.IO`를 직접 쓰지 말고 이 한정자로 주입받는다 (AGENTS.md §5). */
enum class DeskDispatchers {
    Default,
    IO,
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: DeskDispatchers)

/** 앱 프로세스 수명 동안 유지되는 CoroutineScope. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
