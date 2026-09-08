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

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.skydoves.nowinandroid.core.data.sync.initializeSync
import com.skydoves.nowinandroid.di.DesktopAppGraph
import com.skydoves.nowinandroid.ui.NiaAppRoot
import dev.zacsweers.metro.createGraph

fun main() {
    val appGraph = createGraph<DesktopAppGraph>()
    // The desktop has no background scheduler, so the one-off start-up sync is requested here,
    // mirroring the Android original's unique `SyncWorker`.
    appGraph.syncManager.initializeSync()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Now in Android",
            state = rememberWindowState(size = DpSize(width = 1100.dp, height = 800.dp)),
        ) {
            NiaAppRoot(appGraph)
        }
    }
}
