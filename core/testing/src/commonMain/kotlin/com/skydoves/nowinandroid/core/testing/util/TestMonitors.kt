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

package com.skydoves.nowinandroid.core.testing.util

import com.skydoves.nowinandroid.core.data.sync.SyncManager
import com.skydoves.nowinandroid.core.data.util.NetworkMonitor
import com.skydoves.nowinandroid.core.data.util.TimeZoneMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone

class TestNetworkMonitor : NetworkMonitor {
    private val connectivityFlow = MutableStateFlow(true)

    override val isOnline: Flow<Boolean> = connectivityFlow.asStateFlow()

    /**
     * A test-only API to set the connectivity state from tests.
     */
    fun setConnected(isConnected: Boolean) {
        connectivityFlow.value = isConnected
    }
}

class TestTimeZoneMonitor : TimeZoneMonitor {
    private val timeZoneFlow = MutableStateFlow(defaultTimeZone)

    override val currentTimeZone: Flow<TimeZone> = timeZoneFlow.asStateFlow()

    /**
     * A test-only API to set the time zone from tests.
     */
    fun setTimeZone(zoneId: TimeZone) {
        timeZoneFlow.value = zoneId
    }

    companion object {
        val defaultTimeZone: TimeZone = TimeZone.of("Europe/Warsaw")
    }
}

class NeverSyncingSyncManager : SyncManager {
    override val isSyncing: Flow<Boolean> = MutableStateFlow(false)
    override fun requestSync() = Unit
}

class TestSyncManager : SyncManager {

    private val syncStatusFlow = MutableStateFlow(false)

    override val isSyncing: Flow<Boolean> = syncStatusFlow

    override fun requestSync() {
        syncStatusFlow.value = true
    }

    /**
     * A test-only API to set the sync status from tests.
     */
    fun setSyncing(isSyncing: Boolean) {
        syncStatusFlow.value = isSyncing
    }
}
