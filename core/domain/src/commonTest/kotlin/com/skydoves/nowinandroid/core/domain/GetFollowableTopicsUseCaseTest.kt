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

import com.skydoves.nowinandroid.core.domain.TopicSortField.NAME
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.testing.repository.TestTopicsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetFollowableTopicsUseCaseTest {

    private val topicsRepository = TestTopicsRepository()
    private val userDataRepository = TestUserDataRepository()

    private val useCase = GetFollowableTopicsUseCase(topicsRepository, userDataRepository)

    @Test
    fun whenNoParams_followableTopicsAreReturnedWithNoSorting() = runTest {
        val followableTopics = useCase()

        topicsRepository.sendTopics(testTopics)
        userDataRepository.setFollowedTopicIds(setOf(testTopics[0].id, testTopics[2].id))

        assertEquals(
            listOf(
                FollowableTopic(testTopics[0], true),
                FollowableTopic(testTopics[1], false),
                FollowableTopic(testTopics[2], true),
            ),
            followableTopics.first(),
        )
    }

    @Test
    fun whenSortOrderIsByName_topicsSortedByNameAreReturned() = runTest {
        val followableTopics = useCase(sortBy = NAME)

        topicsRepository.sendTopics(testTopics)
        userDataRepository.setFollowedTopicIds(emptySet())

        assertEquals(
            testTopics.sortedBy { it.name }.map { FollowableTopic(it, false) },
            followableTopics.first(),
        )
    }

    @Test
    fun followedStateTracksTheUserDataStream() = runTest {
        val followableTopics = useCase()

        topicsRepository.sendTopics(testTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        assertEquals(listOf(false, false, false), followableTopics.first().map { it.isFollowed })

        userDataRepository.setTopicIdFollowed(testTopics[1].id, true)
        assertEquals(listOf(false, true, false), followableTopics.first().map { it.isFollowed })
    }
}

private val testTopics = listOf(
    Topic("1", "Headlines", "", "", "", ""),
    Topic("2", "Android Studio", "", "", "", ""),
    Topic("3", "Compose", "", "", "", ""),
)
