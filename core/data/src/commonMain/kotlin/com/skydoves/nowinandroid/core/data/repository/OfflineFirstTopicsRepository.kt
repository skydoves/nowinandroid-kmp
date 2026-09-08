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
import com.skydoves.nowinandroid.core.database.dao.TopicDao
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import com.skydoves.nowinandroid.core.database.model.asExternalModel
import com.skydoves.nowinandroid.core.datastore.ChangeListVersions
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.network.NiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.sandwich.getOrThrow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Disk storage backed implementation of the [TopicsRepository].
 * Reads are exclusively from local storage to support offline access.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class OfflineFirstTopicsRepository(private val topicDao: TopicDao, private val network: NiaNetworkDataSource) :
    TopicsRepository {

    override fun getTopics(): Flow<List<Topic>> =
        topicDao.getTopicEntities().map { it.map(TopicEntity::asExternalModel) }

    override fun getTopic(id: String): Flow<Topic> = topicDao.getTopicEntity(id).map { it.asExternalModel() }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = synchronizer.changeListSync(
        versionReader = ChangeListVersions::topicVersion,
        changeListFetcher = { currentVersion ->
            network.getTopicChangeList(after = currentVersion)
        },
        versionUpdater = { latestVersion ->
            copy(topicVersion = latestVersion)
        },
        modelDeleter = topicDao::deleteTopics,
        modelUpdater = { changedIds ->
            val networkTopics = network.getTopics(ids = changedIds).getOrThrow()
            topicDao.upsertTopics(entities = networkTopics.map(NetworkTopic::asEntity))
        },
    )
}
