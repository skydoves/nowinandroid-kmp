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

package com.skydoves.nowinandroid.core.database.model

import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.Topic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class PopulatedNewsResourceKtTest {

    private val populatedNewsResource = PopulatedNewsResource(
        entity = NewsResourceEntity(
            id = "1",
            title = "news",
            content = "Metro",
            url = "url",
            headerImageUrl = "headerImageUrl",
            type = "Video 📺",
            publishDate = Instant.fromEpochMilliseconds(1),
        ),
        topics = listOf(
            TopicEntity(
                id = "3",
                name = "name",
                shortDescription = "short description",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    )

    @Test
    fun populatedNewsResourceCanBeMappedToNewsResource() {
        assertEquals(
            NewsResource(
                id = "1",
                title = "news",
                content = "Metro",
                url = "url",
                headerImageUrl = "headerImageUrl",
                type = "Video 📺",
                publishDate = Instant.fromEpochMilliseconds(1),
                topics = listOf(
                    Topic(
                        id = "3",
                        name = "name",
                        shortDescription = "short description",
                        longDescription = "long description",
                        url = "URL",
                        imageUrl = "image URL",
                    ),
                ),
            ),
            populatedNewsResource.asExternalModel(),
        )
    }

    @Test
    fun populatedNewsResourceCanBeMappedToAnFtsEntity() {
        val fts = populatedNewsResource.asFtsEntity()

        assertEquals("1", fts.newsResourceId)
        assertEquals("news", fts.title)
        assertEquals("Metro", fts.content)
    }
}
