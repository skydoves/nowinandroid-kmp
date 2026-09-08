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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat
import com.skydoves.nowinandroid.core.common.network.ApplicationScope
import com.skydoves.nowinandroid.core.common.network.IoDispatcher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinTimeZone
import java.time.ZoneId

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TimeZoneBroadcastMonitor(
    private val context: Context,
    @ApplicationScope appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TimeZoneMonitor {

    override val currentTimeZone: SharedFlow<TimeZone> = callbackFlow {
        // Send the default time zone first.
        trySend(TimeZone.currentSystemDefault())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != Intent.ACTION_TIMEZONE_CHANGED) return

                val zoneIdFromIntent = if (VERSION.SDK_INT < VERSION_CODES.R) {
                    null
                } else {
                    // Starting Android R the intent carries the new zone id, which saves a
                    // round trip through the (potentially stale) system default.
                    intent.getStringExtra(Intent.EXTRA_TIMEZONE)?.let { zoneId ->
                        runCatching { ZoneId.of(zoneId).toKotlinTimeZone() }.getOrNull()
                    }
                }

                trySend(zoneIdFromIntent ?: TimeZone.currentSystemDefault())
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_TIMEZONE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        awaitClose { context.unregisterReceiver(receiver) }
    }
        .distinctUntilChanged()
        .conflate()
        .flowOn(ioDispatcher)
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}
