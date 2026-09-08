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

import com.skydoves.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.skydoves.nowinandroid.core.data.repository.NewsResourceQuery
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.testing.repository.TestNewsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.repository.emptyUserData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class CompositeUserNewsResourceRepositoryTest {

    private val newsRepository = TestNewsRepository()
    private val userDataRepository = TestUserDataRepository()
    private val subject = CompositeUserNewsResourceRepository(newsRepository, userDataRepository)

    @Test
    fun newsResourcesAreJoinedWithUserData() = runTest {
        newsRepository.sendNewsResources(sampleNewsResources)
        userDataRepository.setUserData(
            emptyUserData.copy(
                followedTopics = setOf("1"),
                bookmarkedNewsResources = setOf("2"),
                viewedNewsResources = setOf("1"),
            ),
        )

        val userNewsResources = subject.observeAll().first()

        assertEquals(2, userNewsResources.size)
        assertTrue(userNewsResources.first { it.id == "1" }.hasBeenViewed)
        assertTrue(userNewsResources.first { it.id == "2" }.isSaved)
        assertTrue(userNewsResources.first { it.id == "1" }.followableTopics.single().isFollowed)
    }

    @Test
    fun onlyFollowedTopicsAreObserved() = runTest {
        newsRepository.sendNewsResources(sampleNewsResources)
        userDataRepository.setUserData(emptyUserData.copy(followedTopics = setOf("2")))

        assertEquals(listOf("2"), subject.observeAllForFollowedTopics().first().map { it.id })
    }

    @Test
    fun observingBookmarksWithNoBookmarksIsEmpty() = runTest {
        newsRepository.sendNewsResources(sampleNewsResources)
        userDataRepository.setUserData(emptyUserData)

        assertEquals(emptyList(), subject.observeAllBookmarked().first())
    }

    @Test
    fun filteringByNewsIdNarrowsTheResult() = runTest {
        newsRepository.sendNewsResources(sampleNewsResources)
        userDataRepository.setUserData(emptyUserData)

        val filtered = subject.observeAll(NewsResourceQuery(filterNewsIds = setOf("2"))).first()

        assertEquals(listOf("2"), filtered.map { it.id })
    }
}

private val topicOne = Topic("1", "Compose", "short", "long", "", "")
private val topicTwo = Topic("2", "Testing", "short", "long", "", "")

private val sampleNewsResources = listOf(
    NewsResource(
        id = "1",
        title = "Compose 1.9",
        content = "content",
        url = "https://example.com/1",
        headerImageUrl = null,
        publishDate = Instant.fromEpochMilliseconds(0),
        type = "Article",
        topics = listOf(topicOne),
    ),
    NewsResource(
        id = "2",
        title = "Testing tips",
        content = "content",
        url = "https://example.com/2",
        headerImageUrl = null,
        publishDate = Instant.fromEpochMilliseconds(1),
        type = "Article",
        topics = listOf(topicTwo),
    ),
)
