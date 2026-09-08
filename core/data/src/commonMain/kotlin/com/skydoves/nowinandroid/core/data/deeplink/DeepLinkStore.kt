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

package com.skydoves.nowinandroid.core.data.deeplink

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Carries a deep-linked news resource id from wherever the platform receives it to the For You
 * screen.
 *
 * The Android original routed this through the Activity's `SavedStateHandle`. There is no Activity
 * on iOS or the desktop, and a notification tap arrives through a completely different API on each,
 * so the app-scoped store is the one thing they can all write to.
 */
@Inject
@SingleIn(AppScope::class)
class DeepLinkStore {
    private val newsResourceIdState = MutableStateFlow<String?>(null)

    val newsResourceId: StateFlow<String?> = newsResourceIdState.asStateFlow()

    fun submit(newsResourceId: String?) {
        newsResourceIdState.value = newsResourceId
    }
}
