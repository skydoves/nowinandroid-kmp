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
import com.skydoves.nowinandroid.core.data.model.topicCrossReferences
import com.skydoves.nowinandroid.core.data.model.topicEntityShells
import com.skydoves.nowinandroid.core.data.testdoubles.CollectionType
import com.skydoves.nowinandroid.core.data.testdoubles.TestNewsResourceDao
import com.skydoves.nowinandroid.core.data.testdoubles.TestNiaNetworkDataSource
import com.skydoves.nowinandroid.core.data.testdoubles.TestTopicDao
import com.skydoves.nowinandroid.core.data.testdoubles.filteredInterestsIds
import com.skydoves.nowinandroid.core.data.testdoubles.nonPresentInterestsIds
import com.skydoves.nowinandroid.core.database.model.NewsResourceEntity
import com.skydoves.nowinandroid.core.database.model.NewsResourceTopicCrossRef
import com.skydoves.nowinandroid.core.database.model.PopulatedNewsResource
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import com.skydoves.nowinandroid.core.database.model.asExternalModel
import com.skydoves.nowinandroid.core.datastore.NiaPreferencesDataSource
import com.skydoves.nowinandroid.core.datastore.UserPreferences
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.network.model.NetworkChangeList
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.testing.notifications.TestNotifier
import com.skydoves.nowinandroid.core.testing.util.InMemoryDataStore
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstNewsRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstNewsRepository
    private lateinit var niaPreferencesDataSource: NiaPreferencesDataSource
    private lateinit var newsResourceDao: TestNewsResourceDao
    private lateinit var topicDao: TestTopicDao
    private lateinit var network: TestNiaNetworkDataSource
    private lateinit var notifier: TestNotifier
    private lateinit var synchronizer: Synchronizer

    @BeforeTest
    fun setup() {
        niaPreferencesDataSource = NiaPreferencesDataSource(InMemoryDataStore(UserPreferences()))
        newsResourceDao = TestNewsResourceDao()
        topicDao = TestTopicDao()
        network = TestNiaNetworkDataSource()
        notifier = TestNotifier()
        synchronizer = TestSynchronizer(niaPreferencesDataSource)

        subject = OfflineFirstNewsRepository(
            niaPreferencesDataSource = niaPreferencesDataSource,
            newsResourceDao = newsResourceDao,
            topicDao = topicDao,
            network = network,
            notifier = notifier,
        )
    }

    @Test
    fun newsResourcesStreamIsBackedByNewsResourceDao() = testScope.runTest {
        subject.syncWith(synchronizer)

        assertEquals(
            newsResourceDao.getNewsResources().first().map(PopulatedNewsResource::asExternalModel),
            subject.getNewsResources().first(),
        )
    }

    @Test
    fun newsResourcesForTopicAreBackedByNewsResourceDao() = testScope.runTest {
        assertEquals(
            expected = newsResourceDao.getNewsResources(
                filterTopicIds = filteredInterestsIds,
                useFilterTopicIds = true,
            ).first().map(PopulatedNewsResource::asExternalModel),
            actual = subject.getNewsResources(
                query = NewsResourceQuery(filterTopicIds = filteredInterestsIds),
            ).first(),
        )

        assertEquals(
            expected = emptyList(),
            actual = subject.getNewsResources(
                query = NewsResourceQuery(filterTopicIds = nonPresentInterestsIds),
            ).first(),
        )
    }

    @Test
    fun syncPullsFromNetwork() = testScope.runTest {
        // User has not onboarded
        niaPreferencesDataSource.setShouldHideOnboarding(false)
        subject.syncWith(synchronizer)

        val newsResourcesFromNetwork = network.getNewsResources().getOrThrow()
            .map(NetworkNewsResource::asEntity)
            .map(NewsResourceEntity::asExternalModel)

        val newsResourcesFromDb = newsResourceDao.getNewsResources().first()
            .map(PopulatedNewsResource::asExternalModel)

        assertEquals(
            newsResourcesFromNetwork.map(NewsResource::id).sorted(),
            newsResourcesFromDb.map(NewsResource::id).sorted(),
        )

        assertEquals(
            expected = network.latestChangeListVersion(CollectionType.NewsResources),
            actual = synchronizer.getChangeListVersions().newsResourceVersion,
        )

        // Notifier should not have been called
        assertTrue(notifier.addedNewsResources.isEmpty())
    }

    @Test
    fun syncDeletesItemsMarkedDeletedOnNetwork() = testScope.runTest {
        niaPreferencesDataSource.setShouldHideOnboarding(false)

        val newsResourcesFromNetwork = network.getNewsResources().getOrThrow()
            .map(NetworkNewsResource::asEntity)
            .map(NewsResourceEntity::asExternalModel)

        // Delete half of the items on the network
        val deletedItems = newsResourcesFromNetwork
            .map(NewsResource::id)
            .partition { id -> id.sumOf(Char::code) % 2 == 0 }
            .first
            .toSet()

        deletedItems.forEach {
            network.editCollection(
                collectionType = CollectionType.NewsResources,
                id = it,
                isDelete = true,
            )
        }

        subject.syncWith(synchronizer)

        val newsResourcesFromDb = newsResourceDao.getNewsResources().first()
            .map(PopulatedNewsResource::asExternalModel)

        assertEquals(
            expected = (newsResourcesFromNetwork.map(NewsResource::id) - deletedItems).sorted(),
            actual = newsResourcesFromDb.map(NewsResource::id).sorted(),
        )
        assertEquals(
            expected = network.latestChangeListVersion(CollectionType.NewsResources),
            actual = synchronizer.getChangeListVersions().newsResourceVersion,
        )
        assertTrue(notifier.addedNewsResources.isEmpty())
    }

    @Test
    fun incrementalSyncPullsFromNetwork() = testScope.runTest {
        niaPreferencesDataSource.setShouldHideOnboarding(false)

        // Set news version to 7
        synchronizer.updateChangeListVersions { copy(newsResourceVersion = 7) }

        subject.syncWith(synchronizer)

        val changeList = network.changeListsAfter(CollectionType.NewsResources, version = 7)
        val changeListIds = changeList.map(NetworkChangeList::id).toSet()

        val newsResourcesFromNetwork = network.getNewsResources().getOrThrow()
            .map(NetworkNewsResource::asEntity)
            .map(NewsResourceEntity::asExternalModel)
            .filter { it.id in changeListIds }

        val newsResourcesFromDb = newsResourceDao.getNewsResources().first()
            .map(PopulatedNewsResource::asExternalModel)

        assertEquals(
            expected = newsResourcesFromNetwork.map(NewsResource::id).sorted(),
            actual = newsResourcesFromDb.map(NewsResource::id).sorted(),
        )
        assertEquals(
            expected = changeList.last().changeListVersion,
            actual = synchronizer.getChangeListVersions().newsResourceVersion,
        )
        assertTrue(notifier.addedNewsResources.isEmpty())
    }

    @Test
    fun syncSavesShellTopicEntities() = testScope.runTest {
        subject.syncWith(synchronizer)

        assertEquals(
            expected = network.getNewsResources().getOrThrow()
                .map(NetworkNewsResource::topicEntityShells)
                .flatten()
                .distinctBy(TopicEntity::id)
                .sortedBy(TopicEntity::toString),
            actual = topicDao.getTopicEntities().first().sortedBy(TopicEntity::toString),
        )
    }

    @Test
    fun syncSavesTopicCrossReferences() = testScope.runTest {
        subject.syncWith(synchronizer)

        assertEquals(
            expected = network.getNewsResources().getOrThrow()
                .map(NetworkNewsResource::topicCrossReferences)
                .flatten()
                .distinct()
                .sortedBy(NewsResourceTopicCrossRef::toString),
            actual = newsResourceDao.topicCrossReferences
                .sortedBy(NewsResourceTopicCrossRef::toString),
        )
    }

    @Test
    fun syncMarksAsReadOnFirstRun() = testScope.runTest {
        subject.syncWith(synchronizer)

        assertEquals(
            network.getNewsResources().getOrThrow().map { it.id }.toSet(),
            niaPreferencesDataSource.userData.first().viewedNewsResources,
        )
    }

    @Test
    fun syncDoesNotMarkAsReadOnSubsequentRun() = testScope.runTest {
        // Pretend that we already have up to change list 7
        synchronizer.updateChangeListVersions { copy(newsResourceVersion = 7) }

        subject.syncWith(synchronizer)

        assertEquals(emptySet(), niaPreferencesDataSource.userData.first().viewedNewsResources)
    }

    @Test
    fun sendsNotificationsForNewlySyncedNewsThatIsFollowed() = testScope.runTest {
        // User has onboarded
        niaPreferencesDataSource.setShouldHideOnboarding(true)

        val networkNewsResources = network.getNewsResources().getOrThrow()

        // Follow roughly half the topics
        val followedTopicIds = networkNewsResources
            .flatMap(NetworkNewsResource::topicEntityShells)
            .mapNotNull { topic ->
                when (topic.id.sumOf(Char::code) % 2) {
                    0 -> topic.id
                    else -> null
                }
            }
            .toSet()

        niaPreferencesDataSource.setFollowedTopicIds(followedTopicIds)

        subject.syncWith(synchronizer)

        val followedNewsResourceIdsFromNetwork = networkNewsResources
            .filter { (it.topics intersect followedTopicIds).isNotEmpty() }
            .map(NetworkNewsResource::id)
            .sorted()

        // Notifier should have been called with only news resources that have topics
        // that the user follows
        assertEquals(
            expected = followedNewsResourceIdsFromNetwork,
            actual = notifier.addedNewsResources.first().map(NewsResource::id).sorted(),
        )
    }

    @Test
    fun doesNotSendNotificationsForExistingNewsResources() = testScope.runTest {
        niaPreferencesDataSource.setShouldHideOnboarding(true)

        val networkNewsResources = network.getNewsResources().getOrThrow()
            .map(NetworkNewsResource::asEntity)
        val newsResources = networkNewsResources.map(NewsResourceEntity::asExternalModel)

        // Prepopulate dao with news resources
        newsResourceDao.upsertNewsResources(networkNewsResources)

        val followedTopicIds = newsResources
            .flatMap(NewsResource::topics)
            .map(Topic::id)
            .toSet()

        niaPreferencesDataSource.setFollowedTopicIds(followedTopicIds)

        subject.syncWith(synchronizer)

        // Notifier should not have been called because all news resources existed previously
        assertTrue(notifier.addedNewsResources.isEmpty())
    }
}
