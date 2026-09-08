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

package com.skydoves.nowinandroid.di

import com.skydoves.nowinandroid.core.analytics.AnalyticsHelper
import com.skydoves.nowinandroid.core.data.deeplink.DeepLinkStore
import com.skydoves.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.skydoves.nowinandroid.core.data.sync.SyncManager
import com.skydoves.nowinandroid.core.data.util.NetworkMonitor
import com.skydoves.nowinandroid.core.data.util.TimeZoneMonitor
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.ktor.client.HttpClient

/**
 * What the UI needs from the object graph. Each platform declares a concrete
 * `@DependencyGraph` that extends this, because only the platform knows how to reach a `Context`,
 * a documents directory, and so on.
 */
interface AppGraph : ViewModelGraph {
    val networkMonitor: NetworkMonitor
    val timeZoneMonitor: TimeZoneMonitor
    val userNewsResourceRepository: UserNewsResourceRepository
    val analyticsHelper: AnalyticsHelper
    val syncManager: SyncManager
    val deepLinkStore: DeepLinkStore
    val httpClient: HttpClient
}
