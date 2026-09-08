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

package com.skydoves.nowinandroid.feature.interests.impl

import com.skydoves.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.testing.repository.TestTopicsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.util.MainDispatcherRule
import com.skydoves.nowinandroid.feature.interests.api.navigation.InterestsNavKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 *
 * The Android original had to run this under Robolectric, because the ViewModel read the selected
 * topic out of a `SavedStateHandle` and that needed `android.os.Bundle`. The selection is plain
 * state here, so the test is ordinary common code.
 */
class InterestsViewModelTest {

    private val dispatcherRule = MainDispatcherRule()
    private val userDataRepository = TestUserDataRepository()
    private val topicsRepository = TestTopicsRepository()
    private val getFollowableTopicsUseCase = GetFollowableTopicsUseCase(
        topicsRepository = topicsRepository,
        userDataRepository = userDataRepository,
    )
    private lateinit var viewModel: InterestsViewModel

    @BeforeTest
    fun setup() {
        dispatcherRule.setUp()
        viewModel = InterestsViewModel(
            userDataRepository = userDataRepository,
            getFollowableTopics = getFollowableTopicsUseCase,
            key = InterestsNavKey(initialTopicId = testInputTopics[0].topic.id),
        )
    }

    @AfterTest
    fun tearDown() = dispatcherRule.tearDown()

    @Test
    fun uiState_whenInitialized_thenShowLoading() = runTest {
        assertEquals(InterestsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_whenFollowedTopicsAreLoading_thenShowLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        userDataRepository.setFollowedTopicIds(emptySet())
        assertEquals(InterestsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_whenFollowingNewTopic_thenShowUpdatedTopics() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val toggleTopicId = testOutputTopics[1].topic.id
        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[0].topic.id))

        assertFalse(
            (viewModel.uiState.value as InterestsUiState.Interests)
                .topics.first { it.topic.id == toggleTopicId }.isFollowed,
        )

        viewModel.followTopic(followedTopicId = toggleTopicId, followed = true)

        assertEquals(
            InterestsUiState.Interests(
                topics = testOutputTopics,
                selectedTopicId = testInputTopics[0].topic.id,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun uiState_whenUnfollowingTopics_thenShowUpdatedTopics() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val toggleTopicId = testOutputTopics[1].topic.id
        topicsRepository.sendTopics(testOutputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(
            setOf(testOutputTopics[0].topic.id, testOutputTopics[1].topic.id),
        )

        assertTrue(
            (viewModel.uiState.value as InterestsUiState.Interests)
                .topics.first { it.topic.id == toggleTopicId }.isFollowed,
        )

        viewModel.followTopic(followedTopicId = toggleTopicId, followed = false)

        assertEquals(
            InterestsUiState.Interests(
                topics = testInputTopics,
                selectedTopicId = testInputTopics[0].topic.id,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun uiState_selectedTopicIsSeededFromTheNavKey() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[0].topic.id))

        assertEquals(
            testInputTopics[0].topic.id,
            (viewModel.uiState.value as InterestsUiState.Interests).selectedTopicId,
        )
    }

    @Test
    fun onTopicClick_updatesTheSelection() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        topicsRepository.sendTopics(testInputTopics.map { it.topic })
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[0].topic.id))

        viewModel.onTopicClick(testInputTopics[2].topic.id)
        assertEquals(
            testInputTopics[2].topic.id,
            (viewModel.uiState.value as InterestsUiState.Interests).selectedTopicId,
        )

        viewModel.onTopicClick(null)
        assertNull((viewModel.uiState.value as InterestsUiState.Interests).selectedTopicId)
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
    FollowableTopic(testTopic("1", TOPIC_2_NAME), isFollowed = true),
    FollowableTopic(testTopic("2", TOPIC_3_NAME), isFollowed = false),
)
