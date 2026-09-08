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
import com.skydoves.nowinandroid.core.data.model.asEntity
import com.skydoves.nowinandroid.core.data.testdoubles.CollectionType
import com.skydoves.nowinandroid.core.data.testdoubles.TestNiaNetworkDataSource
import com.skydoves.nowinandroid.core.data.testdoubles.TestTopicDao
import com.skydoves.nowinandroid.core.database.dao.TopicDao
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import com.skydoves.nowinandroid.core.database.model.asExternalModel
import com.skydoves.nowinandroid.core.datastore.NiaPreferencesDataSource
import com.skydoves.nowinandroid.core.datastore.UserPreferences
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.nowinandroid.core.testing.util.InMemoryDataStore
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OfflineFirstTopicsRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstTopicsRepository
    private lateinit var topicDao: TopicDao
    private lateinit var network: TestNiaNetworkDataSource
    private lateinit var niaPreferences: NiaPreferencesDataSource
    private lateinit var synchronizer: Synchronizer

    @BeforeTest
    fun setup() {
        topicDao = TestTopicDao()
        network = TestNiaNetworkDataSource()
        niaPreferences = NiaPreferencesDataSource(InMemoryDataStore(UserPreferences()))
        synchronizer = TestSynchronizer(niaPreferences)

        subject = OfflineFirstTopicsRepository(topicDao = topicDao, network = network)
    }

    @Test
    fun topicsStreamIsBackedByTopicsDao() = testScope.runTest {
        subject.syncWith(synchronizer)

        assertEquals(
            topicDao.getTopicEntities().first().map(TopicEntity::asExternalModel),
            subject.getTopics().first(),
        )
    }

    @Test
    fun syncPullsFromNetwork() = testScope.runTest {
        subject.syncWith(synchronizer)

        val networkTopics = network.getTopics().getOrThrow().map(NetworkTopic::asEntity)
        val dbTopics = topicDao.getTopicEntities().first()

        assertEquals(networkTopics.map(TopicEntity::id), dbTopics.map(TopicEntity::id))

        // After sync version should be updated
        assertEquals(
            network.latestChangeListVersion(CollectionType.Topics),
            synchronizer.getChangeListVersions().topicVersion,
        )
    }

    @Test
    fun incrementalSyncPullsFromNetwork() = testScope.runTest {
        // Set topics version to 10
        synchronizer.updateChangeListVersions { copy(topicVersion = 10) }

        subject.syncWith(synchronizer)

        val networkTopics = network.getTopics().getOrThrow()
            .map(NetworkTopic::asEntity)
            // Drop 10 to simulate the first 10 items being unchanged
            .drop(10)
        val dbTopics = topicDao.getTopicEntities().first()

        assertEquals(networkTopics.map(TopicEntity::id), dbTopics.map(TopicEntity::id))
        assertEquals(
            network.latestChangeListVersion(CollectionType.Topics),
            synchronizer.getChangeListVersions().topicVersion,
        )
    }

    @Test
    fun syncDeletesItemsMarkedDeletedOnNetwork() = testScope.runTest {
        val networkTopics = network.getTopics().getOrThrow()
            .map(NetworkTopic::asEntity)
            .map(TopicEntity::asExternalModel)

        // Delete half of the items on the network
        val deletedItems = networkTopics
            .map(Topic::id)
            .partition { id -> id.sumOf(Char::code) % 2 == 0 }
            .first
            .toSet()

        deletedItems.forEach {
            network.editCollection(
                collectionType = CollectionType.Topics,
                id = it,
                isDelete = true,
            )
        }

        subject.syncWith(synchronizer)

        val dbTopics = topicDao.getTopicEntities().first().map(TopicEntity::asExternalModel)

        // Assert that items marked deleted on the network have been deleted locally
        assertEquals(networkTopics.map(Topic::id) - deletedItems, dbTopics.map(Topic::id))
        assertEquals(
            network.latestChangeListVersion(CollectionType.Topics),
            synchronizer.getChangeListVersions().topicVersion,
        )
    }

    @Test
    fun getTopicReadsASingleTopicFromTheDao() = testScope.runTest {
        subject.syncWith(synchronizer)

        val first = topicDao.getTopicEntities().first().first()

        assertEquals(first.asExternalModel(), subject.getTopics().first().first { it.id == first.id })
    }
}
