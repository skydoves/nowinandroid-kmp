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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.landscapist.image.LocalLandscapist
import com.skydoves.nowinandroid.core.analytics.LocalAnalyticsHelper
import com.skydoves.nowinandroid.core.designsystem.theme.NiaTheme
import com.skydoves.nowinandroid.core.ui.LocalTimeZone
import com.skydoves.nowinandroid.di.AppGraph
import com.skydoves.nowinandroid.ui.image.rememberNiaLandscapist
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel

/**
 * The single entry point every platform renders: Android's `MainActivity`, the desktop `Window`
 * and the iOS `ComposeUIViewController` all call this.
 */
@Composable
fun NiaAppRoot(appGraph: AppGraph) {
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
        LocalAnalyticsHelper provides appGraph.analyticsHelper,
        LocalLandscapist provides rememberNiaLandscapist(appGraph.httpClient),
    ) {
        val viewModel: NiaAppViewModel = metroViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val appState = rememberNiaAppState(
            networkMonitor = appGraph.networkMonitor,
            userNewsResourceRepository = appGraph.userNewsResourceRepository,
            timeZoneMonitor = appGraph.timeZoneMonitor,
        )
        val currentTimeZone by appState.currentTimeZone.collectAsStateWithLifecycle()

        NiaTheme(
            darkTheme = uiState.shouldUseDarkTheme(isSystemInDarkTheme()),
            androidTheme = uiState.shouldUseAndroidTheme,
            disableDynamicTheming = uiState.shouldDisableDynamicTheming,
        ) {
            CompositionLocalProvider(LocalTimeZone provides currentTimeZone) {
                NiaApp(appState)
            }
        }
    }
}
