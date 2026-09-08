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
import com.skydoves.nowinandroid.core.common.network.IoDispatcher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import java.net.NetworkInterface
import kotlin.time.Duration.Companion.seconds

/**
 * The desktop JVM has no connectivity broadcast, so this polls the network interfaces: online means
 * at least one non-loopback interface is up. That is the same question `ConnectivityManager`
 * answers on Android, just asked on a timer.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DesktopNetworkMonitor(
    @ApplicationScope appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NetworkMonitor {

    override val isOnline: Flow<Boolean> = flow {
        while (true) {
            emit(hasUsableInterface())
            delay(POLL_INTERVAL)
        }
    }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    private fun hasUsableInterface(): Boolean = runCatching {
        NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .any { it.isUp && !it.isLoopback }
    }.getOrDefault(true)

    private companion object {
        val POLL_INTERVAL = 10.seconds
    }
}
