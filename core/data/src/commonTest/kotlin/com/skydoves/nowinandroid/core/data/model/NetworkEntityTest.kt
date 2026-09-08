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

package com.skydoves.nowinandroid.core.data.model

import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.nowinandroid.core.network.model.asExternalModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class NetworkEntityTest {

    @Test
    fun networkTopicMapsToDatabaseModel() {
        val entity = NetworkTopic(
            id = "0",
            name = "Test",
            shortDescription = "short description",
            longDescription = "long description",
            url = "URL",
            imageUrl = "image URL",
        ).asEntity()

        assertEquals("0", entity.id)
        assertEquals("Test", entity.name)
        assertEquals("short description", entity.shortDescription)
        assertEquals("long description", entity.longDescription)
        assertEquals("URL", entity.url)
        assertEquals("image URL", entity.imageUrl)
    }

    @Test
    fun networkNewsResourceMapsToDatabaseModel() {
        val entity = NetworkNewsResource(
            id = "0",
            title = "title",
            content = "content",
            url = "url",
            headerImageUrl = "headerImageUrl",
            publishDate = Instant.fromEpochMilliseconds(1),
            type = "Article 📚",
        ).asEntity()

        assertEquals("0", entity.id)
        assertEquals("title", entity.title)
        assertEquals("content", entity.content)
        assertEquals("url", entity.url)
        assertEquals("headerImageUrl", entity.headerImageUrl)
        assertEquals(Instant.fromEpochMilliseconds(1), entity.publishDate)
        assertEquals("Article 📚", entity.type)
    }

    @Test
    fun networkTopicMapsToExternalModel() {
        val networkTopic = NetworkTopic(
            id = "0",
            name = "Test",
            shortDescription = "short description",
            longDescription = "long description",
            url = "URL",
            imageUrl = "imageUrl",
        )

        assertEquals(
            Topic(
                id = "0",
                name = "Test",
                shortDescription = "short description",
                longDescription = "long description",
                url = "URL",
                imageUrl = "imageUrl",
            ),
            networkTopic.asExternalModel(),
        )
    }

    @Test
    fun networkNewsResourceMapsToExternalModel() {
        val networkNewsResource = NetworkNewsResource(
            id = "0",
            title = "title",
            content = "content",
            url = "url",
            headerImageUrl = "headerImageUrl",
            publishDate = Instant.fromEpochMilliseconds(1),
            type = "Article 📚",
            topics = listOf("1", "2"),
        )

        val networkTopics = listOf(
            NetworkTopic(id = "1", name = "Test 1"),
            NetworkTopic(id = "2", name = "Test 2"),
            NetworkTopic(id = "3", name = "Test 3"),
        )

        val expected = NewsResource(
            id = "0",
            title = "title",
            content = "content",
            url = "url",
            headerImageUrl = "headerImageUrl",
            publishDate = Instant.fromEpochMilliseconds(1),
            type = "Article 📚",
            // Only the topics this resource references, and only those, are attached.
            topics = listOf(
                Topic(id = "1", name = "Test 1", shortDescription = "", longDescription = "", url = "", imageUrl = ""),
                Topic(id = "2", name = "Test 2", shortDescription = "", longDescription = "", url = "", imageUrl = ""),
            ),
        )

        assertEquals(expected, networkNewsResource.asExternalModel(networkTopics))
    }

    @Test
    fun networkNewsResourceProducesShellTopicsAndCrossReferences() {
        val networkNewsResource = NetworkNewsResource(
            id = "0",
            title = "title",
            content = "content",
            url = "url",
            headerImageUrl = "headerImageUrl",
            publishDate = Instant.fromEpochMilliseconds(1),
            type = "Article 📚",
            topics = listOf("1", "2"),
        )

        assertEquals(listOf("1", "2"), networkNewsResource.topicEntityShells().map { it.id })
        assertEquals(
            listOf("0" to "1", "0" to "2"),
            networkNewsResource.topicCrossReferences().map { it.newsResourceId to it.topicId },
        )
    }
}
