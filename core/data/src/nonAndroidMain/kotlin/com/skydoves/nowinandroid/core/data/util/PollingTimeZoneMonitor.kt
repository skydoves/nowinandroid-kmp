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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.seconds

/**
 * Neither the desktop JVM nor iOS gives Kotlin a broadcast for "the time zone changed" that is
 * worth a platform-specific implementation each, so this polls. It emits immediately and then only
 * when the zone actually differs.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PollingTimeZoneMonitor(@ApplicationScope private val appScope: CoroutineScope) : TimeZoneMonitor {

    override val currentTimeZone: Flow<TimeZone> = flow {
        while (true) {
            emit(TimeZone.currentSystemDefault())
            delay(POLL_INTERVAL)
        }
    }
        .distinctUntilChanged()
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    private companion object {
        val POLL_INTERVAL = 30.seconds
    }
}
