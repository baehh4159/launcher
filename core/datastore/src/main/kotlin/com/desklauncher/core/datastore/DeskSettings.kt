package com.desklauncher.core.datastore

/** 앱 설정 (PRD §8.4). UI 타입에 의존하지 않도록 자체 열거형을 쓴다. */
data class DeskSettings(
    val haptic: HapticPreference = HapticPreference.Default,
    val gripPosition: GripPosition = GripPosition.Bottom,
    val theme: ThemePreference = ThemePreference.System,
    val onboardingCompleted: Boolean = false,
    val appDrawerAutoFocusSearch: Boolean = false,
)

enum class HapticPreference { Off, Light, Default }

/** 서류철 스트립 세로 위치 (PRD §3.1). */
enum class GripPosition { Top, Middle, Bottom }

enum class ThemePreference { System, Light, Dark }
