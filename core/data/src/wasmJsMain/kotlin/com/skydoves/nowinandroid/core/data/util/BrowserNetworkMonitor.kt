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

package com.skydoves.nowinandroid.core.data.util

import com.skydoves.nowinandroid.core.common.network.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn

/**
 * The browser reports connectivity directly, so unlike the desktop this needs no polling: seed with
 * `navigator.onLine` and then follow the `online`/`offline` events.
 *
 * `navigator.onLine` only means "the browser has a network interface", not that the network is
 * reachable, which is the same caveat the Android and desktop monitors carry.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class BrowserNetworkMonitor(@ApplicationScope appScope: CoroutineScope) : NetworkMonitor {

    override val isOnline: Flow<Boolean> = callbackFlow {
        trySend(window.navigator.onLine)

        val onOnline: (org.w3c.dom.events.Event) -> Unit = { trySend(true) }
        val onOffline: (org.w3c.dom.events.Event) -> Unit = { trySend(false) }
        window.addEventListener("online", onOnline)
        window.addEventListener("offline", onOffline)

        awaitClose {
            window.removeEventListener("online", onOnline)
            window.removeEventListener("offline", onOffline)
        }
    }
        .distinctUntilChanged()
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}
