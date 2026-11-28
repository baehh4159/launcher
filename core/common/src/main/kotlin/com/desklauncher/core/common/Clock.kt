package com.desklauncher.core.common

/** 현재 시각(epoch millis). 테스트에서 시각을 고정하기 위해 주입한다. */
fun interface Clock {
    fun now(): Long
}
