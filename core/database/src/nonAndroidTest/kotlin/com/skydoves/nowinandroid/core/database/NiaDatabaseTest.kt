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
import com.skydoves.nowinandroid.core.database.model.NewsResourceFtsEntity
import com.skydoves.nowinandroid.core.database.model.NewsResourceTopicCrossRef
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import com.skydoves.nowinandroid.core.database.model.asFtsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Guards the two things that are genuinely new on this platform: Room 3 running against the
 * bundled SQLite driver off Android, and FTS4 being compiled into that bundled build.
 */
class NiaDatabaseTest {

    private val db = Room.inMemoryDatabaseBuilder<NiaDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun populatedNewsResource_joinsItsTopics() = runTest {
        db.topicDao().upsertTopics(listOf(topic("1", "Compose"), topic("2", "Testing")))
        db.newsResourceDao().upsertNewsResources(listOf(newsResource("n1", "Compose 1.9 is out")))
        db.newsResourceDao().insertOrIgnoreTopicCrossRefEntities(
            listOf(NewsResourceTopicCrossRef("n1", "1")),
        )

        val populated = db.newsResourceDao().getNewsResources().first()

        assertEquals(1, populated.size)
        assertEquals(listOf("Compose"), populated.single().topics.map(TopicEntity::name))
    }

    /**
     * FTS4 supports a trailing `*` only, so `"*compose*"` is really a prefix match: the leading
     * asterisk is dropped by the tokenizer. That is also what happens on Android, so the search
     * repository keeps the original's `"*query*"` spelling.
     */
    @Test
    fun ftsSearch_matchesOnPrefix() = runTest {
        db.topicDao().upsertTopics(listOf(topic("1", "Compose"), topic("2", "Testing")))
        db.newsResourceDao().upsertNewsResources(
            listOf(
                newsResource("n1", "Compose 1.9 is out"),
                newsResource("n2", "Room gains a bundled driver"),
            ),
        )

        db.topicFtsDao().insertAll(db.topicDao().getOneOffTopicEntities().map { it.asFtsEntity() })
        db.newsResourceFtsDao().insertAll(
            listOf(
                NewsResourceFtsEntity("n1", "Compose 1.9 is out", "Compose body"),
                NewsResourceFtsEntity("n2", "Room gains a bundled driver", "Room body"),
            ),
        )

        assertEquals(2, db.topicFtsDao().getCount().first())
        assertEquals(listOf("1"), db.topicFtsDao().searchAllTopics("*Compos*").first())
        assertEquals(listOf("n2"), db.newsResourceFtsDao().searchAllNewsResources("*Room*").first())
        assertEquals(emptyList(), db.topicFtsDao().searchAllTopics("*ompos*").first())
    }

    private fun topic(id: String, name: String) = TopicEntity(
        id = id,
        name = name,
        shortDescription = "$name short",
        longDescription = "$name long",
        url = "",
        imageUrl = "",
    )

    private fun newsResource(id: String, title: String) = NewsResourceEntity(
        id = id,
        title = title,
        content = "$title content",
        url = "https://example.com/$id",
        headerImageUrl = null,
        publishDate = Instant.fromEpochMilliseconds(0),
        type = "Article",
    )
}
