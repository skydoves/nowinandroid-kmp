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

import com.skydoves.nowinandroid.core.data.Synchronizer
import com.skydoves.nowinandroid.core.data.repository.NewsRepository
import com.skydoves.nowinandroid.core.data.repository.SearchContentsRepository
import com.skydoves.nowinandroid.core.data.repository.TopicsRepository
import com.skydoves.nowinandroid.core.datastore.ChangeListVersions
import com.skydoves.nowinandroid.core.datastore.NiaPreferencesDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * The body of a sync, shared by every platform.
 *
 * The Android original put this inside a `SyncWorker`. Only the *scheduling* is platform specific,
 * so the work itself lives here and each [SyncManager] decides when to run it.
 */
@Inject
@SingleIn(AppScope::class)
class NiaSynchronizer(
    private val niaPreferences: NiaPreferencesDataSource,
    private val topicRepository: TopicsRepository,
    private val newsRepository: NewsRepository,
    private val searchContentsRepository: SearchContentsRepository,
) : Synchronizer {

    /**
     * Syncs topics and news in parallel, then rebuilds the search index. Returns whether both
     * halves succeeded, so a caller that can retry (WorkManager) knows to.
     */
    suspend fun sync(): Boolean = coroutineScope {
        val syncedSuccessfully = awaitAll(
            async { topicRepository.sync() },
            async { newsRepository.sync() },
        ).all { it }

        if (syncedSuccessfully) {
            searchContentsRepository.populateFtsData()
        }
        syncedSuccessfully
    }

    override suspend fun getChangeListVersions(): ChangeListVersions = niaPreferences.getChangeListVersions()

    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) =
        niaPreferences.updateChangeListVersion(update)
}
