/*
 * Designed and developed by 2026 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.nowinandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.nowinandroid.core.data.repository.UserDataRepository
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import com.skydoves.nowinandroid.core.model.data.UserData
import com.skydoves.nowinandroid.ui.NiaAppUiState.Loading
import com.skydoves.nowinandroid.ui.NiaAppUiState.Success
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Holds the theme the whole app is rendered with. The Android original called this
 * `MainActivityViewModel`; there is no Activity on iOS or the desktop, so it is named for what it
 * actually does.
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(NiaAppViewModel::class)
class NiaAppViewModel(userDataRepository: UserDataRepository) : ViewModel() {
    val uiState: StateFlow<NiaAppUiState> = userDataRepository.userData
        .map { Success(it) }
        .stateIn(
            scope = viewModelScope,
            initialValue = Loading,
            started = SharingStarted.WhileSubscribed(5_000),
        )
}

sealed interface NiaAppUiState {
    data object Loading : NiaAppUiState

    data class Success(val userData: UserData) : NiaAppUiState {
        override val shouldDisableDynamicTheming = !userData.useDynamicColor

        override val shouldUseAndroidTheme: Boolean = when (userData.themeBrand) {
            ThemeBrand.DEFAULT -> false
            ThemeBrand.ANDROID -> true
        }

        override fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) = when (userData.darkThemeConfig) {
            DarkThemeConfig.FOLLOW_SYSTEM -> isSystemDarkTheme
            DarkThemeConfig.LIGHT -> false
            DarkThemeConfig.DARK -> true
        }
    }

    /**
     * Returns `true` if the state wasn't loaded yet and it should keep showing the splash screen.
     */
    fun shouldKeepSplashScreen() = this is Loading

    /**
     * Returns `true` if the dynamic color is disabled.
     */
    val shouldDisableDynamicTheming: Boolean get() = true

    /**
     * Returns `true` if the Android theme should be used.
     */
    val shouldUseAndroidTheme: Boolean get() = false

    /**
     * Returns `true` if dark theme should be used.
     */
    fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) = isSystemDarkTheme
}
