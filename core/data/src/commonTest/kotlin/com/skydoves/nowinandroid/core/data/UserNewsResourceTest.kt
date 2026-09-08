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

package com.skydoves.nowinandroid.core.data

import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig.FOLLOW_SYSTEM
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.ThemeBrand.DEFAULT
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.model.data.UserData
import com.skydoves.nowinandroid.core.model.data.UserNewsResource
import com.skydoves.nowinandroid.core.model.data.mapToUserNewsResources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

class UserNewsResourceTest {

    private val newsResource = NewsResource(
        id = "N1",
        title = "Test news title",
        content = "Test news content",
        url = "Test URL",
        headerImageUrl = "Test image URL",
        publishDate = Clock.System.now(),
        type = "Article 📚",
        topics = listOf(
            Topic("T1", "Topic 1", "short 1", "long 1", "url 1", "image 1"),
            Topic("T2", "Topic 2", "short 2", "long 2", "url 2", "image 2"),
        ),
    )

    private val userData = UserData(
        bookmarkedNewsResources = setOf("N1"),
        viewedNewsResources = setOf("N1"),
        followedTopics = setOf("T1"),
        themeBrand = DEFAULT,
        darkThemeConfig = FOLLOW_SYSTEM,
        useDynamicColor = false,
        shouldHideOnboarding = true,
    )

    /**
     * Given: Some user data and news resources
     * When: They are combined
     * Then: The correct UserNewsResources are constructed
     */
    @Test
    fun userNewsResourcesAreConstructedFromNewsResourcesAndUserData() {
        val userNewsResource = UserNewsResource(newsResource, userData)

        // Check that the simple field mappings have been done correctly.
        assertEquals(newsResource.id, userNewsResource.id)
        assertEquals(newsResource.title, userNewsResource.title)
        assertEquals(newsResource.content, userNewsResource.content)
        assertEquals(newsResource.url, userNewsResource.url)
        assertEquals(newsResource.headerImageUrl, userNewsResource.headerImageUrl)
        assertEquals(newsResource.publishDate, userNewsResource.publishDate)
        assertEquals(newsResource.type, userNewsResource.type)

        // Each Topic is carried over, with the followed state resolved from the user data.
        assertEquals(newsResource.topics.size, userNewsResource.followableTopics.size)
        assertEquals(
            newsResource.topics,
            userNewsResource.followableTopics.map { it.topic },
        )
        assertEquals(
            listOf(true, false),
            userNewsResource.followableTopics.map { it.isFollowed },
        )

        assertTrue(userNewsResource.isSaved)
        assertTrue(userNewsResource.hasBeenViewed)
    }

    @Test
    fun unbookmarkedAndUnviewedResourcesReflectTheUserData() {
        val userNewsResource = UserNewsResource(
            newsResource,
            userData.copy(bookmarkedNewsResources = emptySet(), viewedNewsResources = emptySet()),
        )

        assertFalse(userNewsResource.isSaved)
        assertFalse(userNewsResource.hasBeenViewed)
    }

    @Test
    fun mappingAListAppliesTheSameUserData() {
        val resources = listOf(newsResource, newsResource.copy(id = "N2"))

        val mapped = resources.mapToUserNewsResources(userData)

        assertEquals(listOf("N1", "N2"), mapped.map { it.id })
        assertEquals(listOf(true, false), mapped.map { it.isSaved })
    }
}
