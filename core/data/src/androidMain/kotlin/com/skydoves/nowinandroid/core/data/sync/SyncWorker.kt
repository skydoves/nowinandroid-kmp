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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters

internal val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

/**
 * Syncs the data layer by delegating to the shared [NiaSynchronizer].
 *
 * The Android original used a Hilt `@HiltWorker` plus a `DelegatingWorker` indirection. Metro has
 * no worker integration, so the graph is reached through [SyncWorkerEntryPoint], which the
 * application installs once at start-up.
 */
class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo = applicationContext.syncForegroundInfo()

    override suspend fun doWork(): Result {
        val synchronizer = SyncWorkerEntryPoint.synchronizer
            ?: return Result.retry()

        return if (synchronizer.sync()) Result.success() else Result.retry()
    }

    companion object {
        /**
         * Expedited one time work to sync data on app startup
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<SyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .build()
    }
}

/**
 * WorkManager instantiates workers reflectively, so the only way to hand one a graph-built
 * dependency is a process-wide handle set during `Application.onCreate`.
 */
object SyncWorkerEntryPoint {
    @Volatile
    var synchronizer: NiaSynchronizer? = null
}

internal const val SYNC_WORK_NAME = "SyncWorkName"

/**
 * Enqueues the one-off start-up sync, deduplicated by [SYNC_WORK_NAME].
 */
fun SyncManager.initializeSync() = requestSync()

internal fun Context.enqueueStartUpSync() {
    WorkManager.getInstance(this).enqueueUniqueWork(
        SYNC_WORK_NAME,
        ExistingWorkPolicy.KEEP,
        SyncWorker.startUpSyncWork(),
    )
}
