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

package com.skydoves.nowinandroid

import androidx.compose.ui.window.ComposeUIViewController
import com.skydoves.nowinandroid.core.data.sync.initializeSync
import com.skydoves.nowinandroid.di.IosAppGraph
import com.skydoves.nowinandroid.ui.NiaAppRoot
import dev.zacsweers.metro.createGraph
import platform.UIKit.UIViewController

private val appGraph: IosAppGraph by lazy {
    createGraph<IosAppGraph>().also { it.syncManager.initializeSync() }
}

/**
 * Entry point for the iOS app. `iosApp/ContentView.swift` wraps this in a
 * `UIViewControllerRepresentable`.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController = ComposeUIViewController {
    NiaAppRoot(appGraph)
}

/**
 * Called from `AppDelegate.swift` when the user taps a news notification.
 */
fun submitDeepLink(newsResourceId: String) {
    appGraph.deepLinkStore.submit(newsResourceId)
}
