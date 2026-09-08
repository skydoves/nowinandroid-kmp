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

package com.skydoves.nowinandroid.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.skydoves.nowinandroid.core.database.model.NewsResourceEntity
import com.skydoves.nowinandroid.core.database.model.NewsResourceTopicCrossRef
import com.skydoves.nowinandroid.core.database.model.RecentSearchQueryEntity
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Exercises the real SQL, not a test double: the queries carry `CASE WHEN` filters and a
 * many-to-many join that are easy to get subtly wrong.
 */
class NiaDaoTest {

    private val db = Room.inMemoryDatabaseBuilder<NiaDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

    private val topicDao = db.topicDao()
    private val newsResourceDao = db.newsResourceDao()
    private val recentSearchQueryDao = db.recentSearchQueryDao()

    @AfterTest
    fun tearDown() = db.close()

    // ------------------------------------------------------------------ topics ---

    @Test
    fun insertOrIgnoreKeepsTheExistingRow() = runTest {
        topicDao.insertOrIgnoreTopics(listOf(topic("1", "First")))
        topicDao.insertOrIgnoreTopics(listOf(topic("1", "Second")))

        assertEquals("First", topicDao.getTopicEntities().first().single().name)
    }

    @Test
    fun upsertOverwritesTheExistingRow() = runTest {
        topicDao.insertOrIgnoreTopics(listOf(topic("1", "First")))
        topicDao.upsertTopics(listOf(topic("1", "Second")))

        assertEquals("Second", topicDao.getTopicEntities().first().single().name)
    }

    @Test
    fun getTopicEntitiesByIdFiltersToTheRequestedIds() = runTest {
        topicDao.upsertTopics(listOf(topic("1", "One"), topic("2", "Two"), topic("3", "Three")))

        assertEquals(
            listOf("1", "3"),
            topicDao.getTopicEntities(setOf("1", "3")).first().map { it.id },
        )
    }

    @Test
    fun getTopicEntityStreamsASingleTopic() = runTest {
        topicDao.upsertTopics(listOf(topic("1", "One"), topic("2", "Two")))

        assertEquals("Two", topicDao.getTopicEntity("2").first().name)
    }

    @Test
    fun deleteTopicsRemovesOnlyTheGivenIds() = runTest {
        topicDao.upsertTopics(listOf(topic("1", "One"), topic("2", "Two")))

        topicDao.deleteTopics(listOf("1"))

        assertEquals(listOf("2"), topicDao.getTopicEntities().first().map { it.id })
    }

    // ---------------------------------------------------------- news resources ---

    @Test
    fun newsResourcesComeBackNewestFirst() = runTest {
        newsResourceDao.upsertNewsResources(
            listOf(newsResource("1", millis = 10), newsResource("2", millis = 30), newsResource("3", millis = 20)),
        )

        assertEquals(
            listOf("2", "3", "1"),
            newsResourceDao.getNewsResources().first().map { it.entity.id },
        )
    }

    @Test
    fun filteringByTopicIdUsesTheCrossReferenceTable() = runTest {
        topicDao.upsertTopics(listOf(topic("t1", "One"), topic("t2", "Two")))
        newsResourceDao.upsertNewsResources(listOf(newsResource("n1"), newsResource("n2")))
        newsResourceDao.insertOrIgnoreTopicCrossRefEntities(
            listOf(NewsResourceTopicCrossRef("n1", "t1"), NewsResourceTopicCrossRef("n2", "t2")),
        )

        assertEquals(
            listOf("n1"),
            newsResourceDao.getNewsResources(useFilterTopicIds = true, filterTopicIds = setOf("t1"))
                .first().map { it.entity.id },
        )
    }

    @Test
    fun filteringByNewsIdAndTopicIdIsAnIntersection() = runTest {
        topicDao.upsertTopics(listOf(topic("t1", "One")))
        newsResourceDao.upsertNewsResources(listOf(newsResource("n1"), newsResource("n2")))
        newsResourceDao.insertOrIgnoreTopicCrossRefEntities(
            listOf(NewsResourceTopicCrossRef("n1", "t1"), NewsResourceTopicCrossRef("n2", "t1")),
        )

        assertEquals(
            listOf("n2"),
            newsResourceDao.getNewsResources(
                useFilterTopicIds = true,
                filterTopicIds = setOf("t1"),
                useFilterNewsIds = true,
                filterNewsIds = setOf("n2"),
            ).first().map { it.entity.id },
        )
    }

    @Test
    fun noFiltersReturnsEverything() = runTest {
        newsResourceDao.upsertNewsResources(listOf(newsResource("n1"), newsResource("n2")))

        assertEquals(2, newsResourceDao.getNewsResources().first().size)
        assertEquals(2, newsResourceDao.getNewsResourceIds().first().size)
    }

    @Test
    fun deletingANewsResourceCascadesToItsCrossReferences() = runTest {
        topicDao.upsertTopics(listOf(topic("t1", "One")))
        newsResourceDao.upsertNewsResources(listOf(newsResource("n1")))
        newsResourceDao.insertOrIgnoreTopicCrossRefEntities(
            listOf(NewsResourceTopicCrossRef("n1", "t1")),
        )

        newsResourceDao.deleteNewsResources(listOf("n1"))

        assertTrue(newsResourceDao.getNewsResources().first().isEmpty())
    }

    // ---------------------------------------------------------- recent searches ---

    @Test
    fun recentSearchesComeBackMostRecentFirstAndAreLimited() = runTest {
        recentSearchQueryDao.insertOrReplaceRecentSearchQuery(
            RecentSearchQueryEntity("first", Instant.fromEpochMilliseconds(1)),
        )
        recentSearchQueryDao.insertOrReplaceRecentSearchQuery(
            RecentSearchQueryEntity("second", Instant.fromEpochMilliseconds(3)),
        )
        recentSearchQueryDao.insertOrReplaceRecentSearchQuery(
            RecentSearchQueryEntity("third", Instant.fromEpochMilliseconds(2)),
        )

        assertEquals(
            listOf("second", "third"),
            recentSearchQueryDao.getRecentSearchQueryEntities(limit = 2).first().map { it.query },
        )
    }

    @Test
    fun insertingTheSameQueryReplacesIt() = runTest {
        recentSearchQueryDao.insertOrReplaceRecentSearchQuery(
            RecentSearchQueryEntity("kotlin", Instant.fromEpochMilliseconds(1)),
        )
        recentSearchQueryDao.insertOrReplaceRecentSearchQuery(
            RecentSearchQueryEntity("kotlin", Instant.fromEpochMilliseconds(9)),
        )

        val queries = recentSearchQueryDao.getRecentSearchQueryEntities(limit = 10).first()
        assertEquals(1, queries.size)
        assertEquals(Instant.fromEpochMilliseconds(9), queries.single().queriedDate)
    }

    @Test
    fun clearingRecentSearchesEmptiesTheTable() = runTest {
        recentSearchQueryDao.insertOrReplaceRecentSearchQuery(
            RecentSearchQueryEntity("kotlin", Instant.fromEpochMilliseconds(1)),
        )

        recentSearchQueryDao.clearRecentSearchQueries()

        assertTrue(recentSearchQueryDao.getRecentSearchQueryEntities(limit = 10).first().isEmpty())
    }

    private fun topic(id: String, name: String) = TopicEntity(
        id = id,
        name = name,
        shortDescription = "$name short",
        longDescription = "$name long",
        url = "",
        imageUrl = "",
    )

    private fun newsResource(id: String, millis: Long = 0) = NewsResourceEntity(
        id = id,
        title = "Title $id",
        content = "Content $id",
        url = "https://example.com/$id",
        headerImageUrl = null,
        publishDate = Instant.fromEpochMilliseconds(millis),
        type = "Article",
    )
}
