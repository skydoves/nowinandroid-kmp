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

package com.skydoves.nowinandroid.core.domain

import com.skydoves.nowinandroid.core.testing.data.newsResourcesTestData
import com.skydoves.nowinandroid.core.testing.data.topicsTestData
import com.skydoves.nowinandroid.core.testing.repository.TestSearchContentsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.repository.emptyUserData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSearchContentsUseCaseTest {

    private val searchContentsRepository = TestSearchContentsRepository()
    private val userDataRepository = TestUserDataRepository()
    private val useCase = GetSearchContentsUseCase(searchContentsRepository, userDataRepository)

    @BeforeTest
    fun setup() {
        searchContentsRepository.addTopics(topicsTestData)
        searchContentsRepository.addNewsResources(newsResourcesTestData)
        userDataRepository.setUserData(emptyUserData)
    }

    @Test
    fun searchResultsAreJoinedWithUserData() = runTest {
        userDataRepository.setFollowedTopicIds(setOf(topicsTestData.first().id))

        val result = useCase("Headlines").first()

        assertEquals(listOf("Headlines"), result.topics.map { it.topic.name })
        assertTrue(result.topics.single().isFollowed)
    }

    @Test
    fun newsResourcesCarryBookmarkState() = runTest {
        val bookmarked = newsResourcesTestData.first()
        userDataRepository.setNewsResourceBookmarked(bookmarked.id, true)

        val result = useCase(bookmarked.title).first()

        assertEquals(listOf(bookmarked.id), result.newsResources.map { it.id })
        assertTrue(result.newsResources.single().isSaved)
    }

    @Test
    fun aQueryThatMatchesNothingReturnsEmptyResults() = runTest {
        val result = useCase("no such content anywhere").first()

        assertTrue(result.topics.isEmpty())
        assertTrue(result.newsResources.isEmpty())
    }
}
