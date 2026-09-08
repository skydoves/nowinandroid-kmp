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

package com.skydoves.nowinandroid.core.data.sync

import com.skydoves.nowinandroid.core.common.network.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Desktop and iOS have no WorkManager. Sync is instead a coroutine on the application scope,
 * serialised by a mutex so two syncs can never overlap, the invariant [NiaSynchronizer] relies on.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class CoroutineSyncManager(
    @ApplicationScope private val appScope: CoroutineScope,
    private val synchronizer: NiaSynchronizer,
) : SyncManager {

    private val syncing = MutableStateFlow(false)
    private val mutex = Mutex()

    override val isSyncing: Flow<Boolean> = syncing.asStateFlow()

    override fun requestSync() {
        appScope.launch {
            mutex.withLock {
                syncing.value = true
                try {
                    synchronizer.sync()
                } finally {
                    syncing.value = false
                }
            }
        }
    }
}
