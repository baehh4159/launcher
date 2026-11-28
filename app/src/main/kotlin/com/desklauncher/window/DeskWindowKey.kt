package com.desklauncher.window

/** 책상 위에 띄울 창. enum이라 rememberSaveable로 그대로 복원된다. */
enum class DeskWindowKey {
    Apps,
    Settings,
    // 아직 만들지 않은 창 — 해당 기능 ID에서 실제 창으로 바뀐다
    SoonNotebook,
    SoonAll,
    SoonTools,
    SoonQuickWrite,
    SoonResume,
}
