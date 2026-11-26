package com.desklauncher.feature.settings.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** 저장이 끝난 뒤 [onSaved] — 저장 전에 화면을 닫으면 다음 실행에 다시 보일 수 있다. */
    fun complete(onSaved: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onSaved()
        }
    }
}
