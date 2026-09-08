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

package com.skydoves.nowinandroid.core.data.repository

import com.skydoves.nowinandroid.core.data.Synchronizer
import com.skydoves.nowinandroid.core.data.changeListSync
import com.skydoves.nowinandroid.core.data.model.asEntity
import com.skydoves.nowinandroid.core.data.model.topicCrossReferences
import com.skydoves.nowinandroid.core.data.model.topicEntityShells
import com.skydoves.nowinandroid.core.database.dao.NewsResourceDao
import com.skydoves.nowinandroid.core.database.dao.TopicDao
import com.skydoves.nowinandroid.core.database.model.PopulatedNewsResource
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import com.skydoves.nowinandroid.core.database.model.asExternalModel
import com.skydoves.nowinandroid.core.datastore.ChangeListVersions
import com.skydoves.nowinandroid.core.datastore.NiaPreferencesDataSource
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.network.NiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.notifications.Notifier
import com.skydoves.sandwich.getOrThrow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Heuristic value to optimize for serialization and deserialization cost on client and server
// for each news resource batch.
private const val SYNC_BATCH_SIZE = 40

/**
 * Disk storage backed implementation of the [NewsRepository].
 * Reads are exclusively from local storage to support offline access.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class OfflineFirstNewsRepository(
    private val niaPreferencesDataSource: NiaPreferencesDataSource,
    private val newsResourceDao: NewsResourceDao,
    private val topicDao: TopicDao,
    private val network: NiaNetworkDataSource,
    private val notifier: Notifier,
) : NewsRepository {

    override fun getNewsResources(query: NewsResourceQuery): Flow<List<NewsResource>> =
        newsResourceDao.getNewsResources(
            useFilterTopicIds = query.filterTopicIds != null,
            filterTopicIds = query.filterTopicIds ?: emptySet(),
            useFilterNewsIds = query.filterNewsIds != null,
            filterNewsIds = query.filterNewsIds ?: emptySet(),
        ).map { it.map(PopulatedNewsResource::asExternalModel) }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        var isFirstSync = false
        return synchronizer.changeListSync(
            versionReader = ChangeListVersions::newsResourceVersion,
            changeListFetcher = { currentVersion ->
                isFirstSync = currentVersion <= 0
                network.getNewsResourceChangeList(after = currentVersion)
            },
            versionUpdater = { latestVersion ->
                copy(newsResourceVersion = latestVersion)
            },
            modelDeleter = newsResourceDao::deleteNewsResources,
            modelUpdater = { changedIds ->
                val userData = niaPreferencesDataSource.userData.first()
                val hasOnboarded = userData.shouldHideOnboarding
                val followedTopicIds = userData.followedTopics

                val existingNewsResourceIdsThatHaveChanged = when {
                    hasOnboarded -> newsResourceDao.getNewsResourceIds(
                        useFilterTopicIds = true,
                        filterTopicIds = followedTopicIds,
                        useFilterNewsIds = true,
                        filterNewsIds = changedIds.toSet(),
                    ).first().toSet()
                    // No need to retrieve anything if notifications won't be sent
                    else -> emptySet()
                }

                if (isFirstSync) {
                    // When we first retrieve news, mark everything viewed, so that we aren't
                    // overwhelmed with all historical news.
                    niaPreferencesDataSource.setNewsResourcesViewed(changedIds, true)
                }

                // Obtain the news resources which have changed from the network and upsert them
                // locally
                changedIds.chunked(SYNC_BATCH_SIZE).forEach { chunkedIds ->
                    val networkNewsResources = network.getNewsResources(ids = chunkedIds)
                        .getOrThrow()

                    // Order of invocation matters to satisfy id and foreign key constraints!

                    topicDao.insertOrIgnoreTopics(
                        topicEntities = networkNewsResources
                            .map(NetworkNewsResource::topicEntityShells)
                            .flatten()
                            .distinctBy(TopicEntity::id),
                    )
                    newsResourceDao.upsertNewsResources(
                        newsResourceEntities = networkNewsResources.map(
                            NetworkNewsResource::asEntity,
                        ),
                    )
                    newsResourceDao.insertOrIgnoreTopicCrossRefEntities(
                        newsResourceTopicCrossReferences = networkNewsResources
                            .map(NetworkNewsResource::topicCrossReferences)
                            .distinct()
                            .flatten(),
                    )
                }

                if (hasOnboarded) {
                    val addedNewsResources = newsResourceDao.getNewsResources(
                        useFilterTopicIds = true,
                        filterTopicIds = followedTopicIds,
                        useFilterNewsIds = true,
                        filterNewsIds = changedIds.toSet() - existingNewsResourceIdsThatHaveChanged,
                    ).first().map(PopulatedNewsResource::asExternalModel)

                    if (addedNewsResources.isNotEmpty()) {
                        notifier.postNewsNotifications(newsResources = addedNewsResources)
                    }
                }
            },
        )
    }
}
