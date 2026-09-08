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

package com.skydoves.nowinandroid.feature.topic.impl

import com.skydoves.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.testing.repository.TestNewsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestTopicsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class TopicViewModelTest {

    private val dispatcherRule = MainDispatcherRule()
    private val userDataRepository = TestUserDataRepository()
    private val topicsRepository = TestTopicsRepository()
    private val newsRepository = TestNewsRepository()
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )
    private lateinit var viewModel: TopicViewModel

    @BeforeTest
    fun setup() {
        dispatcherRule.setUp()
        viewModel = TopicViewModel(
            userDataRepository = userDataRepository,
            topicsRepository = topicsRepository,
            userNewsResourceRepository = userNewsResourceRepository,
            topicId = testInputTopics[0].topic.id,
        )
    }

    @AfterTest
    fun tearDown() = dispatcherRule.tearDown()

    @Test
    fun topicId_matchesTopicIdFromTheNavKey() = assertEquals(testInputTopics[0].topic.id, viewModel.topicId)

    @Test
    fun uiStateTopic_whenSuccess_matchesTopicFromRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map(FollowableTopic::topic))
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))
        val item = viewModel.topicUiState.value
        assertIs<TopicUiState.Success>(item)

        val topicFromRepository = topicsRepository.getTopic(testInputTopics[0].topic.id).first()

        assertEquals(topicFromRepository, item.followableTopic.topic)
    }

    @Test
    fun uiStateNews_whenInitialized_thenShowLoading() = runTest {
        assertEquals(NewsUiState.Loading, viewModel.newsUiState.value)
    }

    @Test
    fun uiStateTopic_whenInitialized_thenShowLoading() = runTest {
        assertEquals(TopicUiState.Loading, viewModel.topicUiState.value)
    }

    @Test
    fun uiStateTopic_whenFollowedIdsSuccessAndTopicLoading_thenShowLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }

        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))
        assertEquals(TopicUiState.Loading, viewModel.topicUiState.value)
    }

    @Test
    fun uiStateTopic_whenTopicSuccess_thenNewsStillLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))

        assertIs<TopicUiState.Success>(viewModel.topicUiState.value)
        assertIs<NewsUiState.Loading>(viewModel.newsUiState.value)
    }

    @Test
    fun uiStateTopic_whenTopicAndNewsSuccess_thenAllSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            combine(viewModel.topicUiState, viewModel.newsUiState, ::Pair).collect()
        }
        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))
        newsRepository.sendNewsResources(sampleNewsResources)

        assertIs<TopicUiState.Success>(viewModel.topicUiState.value)
        assertIs<NewsUiState.Success>(viewModel.newsUiState.value)
    }

    @Test
    fun uiStateTopic_whenFollowingTopic_thenShowUpdatedTopic() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        // Set which topic IDs are followed, not including 0.
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))

        viewModel.followTopicToggle(true)

        assertEquals(
            TopicUiState.Success(followableTopic = testOutputTopics[0]),
            viewModel.topicUiState.value,
        )
    }

    @Test
    fun bookmarkingNews_updatesTheUserData() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[0].topic.id))
        newsRepository.sendNewsResources(sampleNewsResources)

        viewModel.bookmarkNews(sampleNewsResources.first().id, true)

        assertTrue(
            sampleNewsResources.first().id in
                userDataRepository.getCurrentUserData().bookmarkedNewsResources,
        )
    }

    @Test
    fun markingNewsViewed_updatesTheUserData() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[0].topic.id))
        newsRepository.sendNewsResources(sampleNewsResources)

        viewModel.setNewsResourceViewed(sampleNewsResources.first().id, true)

        assertTrue(
            sampleNewsResources.first().id in
                userDataRepository.getCurrentUserData().viewedNewsResources,
        )
    }
}

private const val TOPIC_1_NAME = "Android Studio"
private const val TOPIC_2_NAME = "Build"
private const val TOPIC_3_NAME = "Compose"
private const val TOPIC_SHORT_DESC = "At vero eos et accusamus."
private const val TOPIC_LONG_DESC = "At vero eos et accusamus et iusto odio dignissimos ducimus."
private const val TOPIC_URL = "URL"
private const val TOPIC_IMAGE_URL = "Image URL"

private fun testTopic(id: String, name: String) = Topic(
    id = id,
    name = name,
    shortDescription = TOPIC_SHORT_DESC,
    longDescription = TOPIC_LONG_DESC,
    url = TOPIC_URL,
    imageUrl = TOPIC_IMAGE_URL,
)

private val testInputTopics = listOf(
    FollowableTopic(testTopic("0", TOPIC_1_NAME), isFollowed = true),
    FollowableTopic(testTopic("1", TOPIC_2_NAME), isFollowed = false),
    FollowableTopic(testTopic("2", TOPIC_3_NAME), isFollowed = false),
)

private val testOutputTopics = listOf(
    FollowableTopic(testTopic("0", TOPIC_1_NAME), isFollowed = true),
    FollowableTopic(testTopic("1", TOPIC_2_NAME), isFollowed = false),
    FollowableTopic(testTopic("2", TOPIC_3_NAME), isFollowed = false),
)

private val sampleNewsResources = listOf(
    NewsResource(
        id = "1",
        title = "Thanks for helping us reach 1M YouTube Subscribers",
        content = "Thank you everyone for following the Now in Android series and everything the " +
            "Android Developers YouTube channel has to offer.",
        url = "https://youtu.be/-fJ6poHQrjM",
        headerImageUrl = "https://i.ytimg.com/vi/-fJ6poHQrjM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-09T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(testInputTopics[0].topic),
    ),
)
