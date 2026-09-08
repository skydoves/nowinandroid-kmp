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

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [SyncManager] backed by [WorkInfo] from [WorkManager]
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class WorkManagerSyncManager(private val context: Context, private val synchronizer: NiaSynchronizer) : SyncManager {

    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { workInfos -> workInfos.anyRunning }

    override fun requestSync() {
        SyncWorkerEntryPoint.synchronizer = synchronizer
        context.enqueueStartUpSync()
    }
}

private val List<WorkInfo>.anyRunning get() = any { it.state == WorkInfo.State.RUNNING }
