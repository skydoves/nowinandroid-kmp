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

package com.skydoves.nowinandroid.feature.foryou.impl

import com.skydoves.nowinandroid.core.analytics.AnalyticsEvent
import com.skydoves.nowinandroid.core.analytics.AnalyticsEvent.Param
import com.skydoves.nowinandroid.core.data.deeplink.DeepLinkStore
import com.skydoves.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.skydoves.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.Topic
import com.skydoves.nowinandroid.core.model.data.UserNewsResource
import com.skydoves.nowinandroid.core.model.data.mapToUserNewsResources
import com.skydoves.nowinandroid.core.notifications.DEEP_LINK_NEWS_RESOURCE_ID_KEY
import com.skydoves.nowinandroid.core.testing.repository.TestNewsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestTopicsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.repository.emptyUserData
import com.skydoves.nowinandroid.core.testing.util.MainDispatcherRule
import com.skydoves.nowinandroid.core.testing.util.TestAnalyticsHelper
import com.skydoves.nowinandroid.core.testing.util.TestSyncManager
import com.skydoves.nowinandroid.core.ui.NewsFeedUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class ForYouViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()
    private val syncManager = TestSyncManager()
    private val analyticsHelper = TestAnalyticsHelper()
    private val userDataRepository = TestUserDataRepository()
    private val topicsRepository = TestTopicsRepository()
    private val newsRepository = TestNewsRepository()
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )
    private val getFollowableTopicsUseCase = GetFollowableTopicsUseCase(
        topicsRepository = topicsRepository,
        userDataRepository = userDataRepository,
    )
    private val deepLinkStore = DeepLinkStore()

    private lateinit var viewModel: ForYouViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setUp()
        viewModel = ForYouViewModel(
            syncManager = syncManager,
            deepLinkStore = deepLinkStore,
            analyticsHelper = analyticsHelper,
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
            getFollowableTopics = getFollowableTopicsUseCase,
        )
    }

    @AfterTest
    fun tearDown() = mainDispatcherRule.tearDown()

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(OnboardingUiState.Loading, viewModel.onboardingUiState.value)
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)
    }

    @Test
    fun stateIsLoadingWhenFollowedTopicsAreLoading() = runTest {
        collectState()

        topicsRepository.sendTopics(sampleTopics)

        assertEquals(OnboardingUiState.Loading, viewModel.onboardingUiState.value)
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)
    }

    @Test
    fun stateIsLoadingWhenAppIsSyncingWithNoInterests() = runTest {
        syncManager.setSyncing(true)

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isSyncing.collect() }

        assertTrue(viewModel.isSyncing.value)
    }

    @Test
    fun onboardingStateIsLoadingWhenTopicsAreLoading() = runTest {
        collectState()

        userDataRepository.setFollowedTopicIds(emptySet())

        assertEquals(OnboardingUiState.Loading, viewModel.onboardingUiState.value)
        assertEquals(NewsFeedUiState.Success(emptyList()), viewModel.feedState.value)
    }

    @Test
    fun onboardingIsShownWhenNewsResourcesAreLoading() = runTest {
        collectState()

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())

        assertEquals(
            OnboardingUiState.Shown(topics = sampleTopics.map { FollowableTopic(it, false) }),
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Success(feed = emptyList()), viewModel.feedState.value)
    }

    @Test
    fun onboardingIsShownAfterLoadingEmptyFollowedTopics() = runTest {
        collectState()

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(
            OnboardingUiState.Shown(topics = sampleTopics.map { FollowableTopic(it, false) }),
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Success(feed = emptyList()), viewModel.feedState.value)
    }

    @Test
    fun onboardingIsNotShownAfterUserDismissesOnboarding() = runTest {
        collectState()

        topicsRepository.sendTopics(sampleTopics)

        val userData = emptyUserData.copy(followedTopics = setOf("0", "1"))
        userDataRepository.setUserData(userData)
        viewModel.dismissOnboarding()

        assertEquals(OnboardingUiState.NotShown, viewModel.onboardingUiState.value)
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)

        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(OnboardingUiState.NotShown, viewModel.onboardingUiState.value)
        assertEquals(
            NewsFeedUiState.Success(feed = sampleNewsResources.mapToUserNewsResources(userData)),
            viewModel.feedState.value,
        )
    }

    @Test
    fun topicSelectionUpdatesAfterSelectingTopic() = runTest {
        collectState()

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(
            OnboardingUiState.Shown(topics = sampleTopics.map { FollowableTopic(it, false) }),
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Success(feed = emptyList()), viewModel.feedState.value)

        val followedTopicId = sampleTopics[1].id
        viewModel.updateTopicSelection(followedTopicId, isChecked = true)

        assertEquals(
            OnboardingUiState.Shown(
                topics = sampleTopics.map { FollowableTopic(it, it.id == followedTopicId) },
            ),
            viewModel.onboardingUiState.value,
        )

        val userData = emptyUserData.copy(followedTopics = setOf(followedTopicId))
        assertEquals(
            NewsFeedUiState.Success(
                feed = listOf(
                    UserNewsResource(sampleNewsResources[1], userData),
                    UserNewsResource(sampleNewsResources[2], userData),
                ),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun topicSelectionUpdatesAfterUnselectingTopic() = runTest {
        collectState()

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        newsRepository.sendNewsResources(sampleNewsResources)
        viewModel.updateTopicSelection("1", isChecked = true)
        viewModel.updateTopicSelection("1", isChecked = false)

        advanceUntilIdle()

        assertEquals(
            OnboardingUiState.Shown(topics = sampleTopics.map { FollowableTopic(it, false) }),
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Success(feed = emptyList()), viewModel.feedState.value)
    }

    @Test
    fun newsResourceSelectionUpdatesAfterLoadingFollowedTopics() = runTest {
        collectState()

        val userData = emptyUserData.copy(
            followedTopics = setOf("1"),
            shouldHideOnboarding = true,
        )

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setUserData(userData)
        newsRepository.sendNewsResources(sampleNewsResources)

        val bookmarkedNewsResourceId = "2"
        viewModel.updateNewsResourceSaved(
            newsResourceId = bookmarkedNewsResourceId,
            isChecked = true,
        )

        val userDataExpected = userData.copy(
            bookmarkedNewsResources = setOf(bookmarkedNewsResourceId),
        )

        assertEquals(OnboardingUiState.NotShown, viewModel.onboardingUiState.value)
        assertEquals(
            NewsFeedUiState.Success(
                feed = listOf(
                    UserNewsResource(newsResource = sampleNewsResources[1], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[2], userDataExpected),
                ),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun deepLinkedNewsResourceIsFetchedAndResetAfterViewing() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.deepLinkedNewsResource.collect()
        }

        newsRepository.sendNewsResources(sampleNewsResources)
        userDataRepository.setUserData(emptyUserData)
        // The Android original wrote this to the Activity's SavedStateHandle; here the platform
        // entry point writes it to the shared store instead.
        deepLinkStore.submit(sampleNewsResources.first().id)

        assertEquals(
            expected = UserNewsResource(
                newsResource = sampleNewsResources.first(),
                userData = emptyUserData,
            ),
            actual = viewModel.deepLinkedNewsResource.value,
        )

        viewModel.onDeepLinkOpened(newsResourceId = sampleNewsResources.first().id)

        assertNull(viewModel.deepLinkedNewsResource.value)

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    type = "news_deep_link_opened",
                    extras = listOf(
                        Param(
                            key = DEEP_LINK_NEWS_RESOURCE_ID_KEY,
                            value = sampleNewsResources.first().id,
                        ),
                    ),
                ),
            ),
        )
    }

    @Test
    fun whenUpdateNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123"
        viewModel.updateNewsResourceSaved(newsResourceId, true)

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )

        viewModel.updateNewsResourceSaved(newsResourceId, false)

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
    }

    @Test
    fun markingAResourceViewedUpdatesTheUserData() = runTest {
        viewModel.setNewsResourceViewed("1", true)

        assertEquals(setOf("1"), userDataRepository.userData.first().viewedNewsResources)
    }

    private fun kotlinx.coroutines.test.TestScope.collectState() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
    }
}

private fun sampleTopic(id: String, name: String) = Topic(
    id = id,
    name = name,
    shortDescription = "",
    longDescription = "long description",
    url = "URL",
    imageUrl = "image URL",
)

private val sampleTopics = listOf(
    sampleTopic("0", "Headlines"),
    sampleTopic("1", "UI"),
    sampleTopic("2", "Tools"),
)

private val sampleNewsResources = listOf(
    NewsResource(
        id = "1",
        title = "Thanks for helping us reach 1M YouTube Subscribers",
        content = "Thank you everyone for following the Now in Android series.",
        url = "https://youtu.be/-fJ6poHQrjM",
        headerImageUrl = "https://i.ytimg.com/vi/-fJ6poHQrjM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-09T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(sampleTopics[0]),
    ),
    NewsResource(
        id = "2",
        title = "Transformations and customisations in the Paging Library",
        content = "A demonstration of different operations that can be performed with Paging.",
        url = "https://youtu.be/ZARz0pjm5YM",
        headerImageUrl = "https://i.ytimg.com/vi/ZARz0pjm5YM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-01T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(sampleTopics[1]),
    ),
    NewsResource(
        id = "3",
        title = "Community tip on Paging",
        content = "Tips for using the Paging library from the developer community.",
        url = "https://youtu.be/r5JgIyS3t3s",
        headerImageUrl = "https://i.ytimg.com/vi/r5JgIyS3t3s/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-08T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(sampleTopics[1]),
    ),
)
