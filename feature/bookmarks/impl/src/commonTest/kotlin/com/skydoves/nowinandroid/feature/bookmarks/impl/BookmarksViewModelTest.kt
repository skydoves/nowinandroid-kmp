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

package com.skydoves.nowinandroid.feature.bookmarks.impl

import com.skydoves.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.skydoves.nowinandroid.core.testing.data.newsResourcesTestData
import com.skydoves.nowinandroid.core.testing.repository.TestNewsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.util.MainDispatcherRule
import com.skydoves.nowinandroid.core.ui.NewsFeedUiState.Loading
import com.skydoves.nowinandroid.core.ui.NewsFeedUiState.Success
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class BookmarksViewModelTest {

    private val dispatcherRule = MainDispatcherRule()
    private val userDataRepository = TestUserDataRepository()
    private val newsRepository = TestNewsRepository()
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )
    private lateinit var viewModel: BookmarksViewModel

    @BeforeTest
    fun setup() {
        dispatcherRule.setUp()
        viewModel = BookmarksViewModel(
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
        )
    }

    @AfterTest
    fun tearDown() = dispatcherRule.tearDown()

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(Loading, viewModel.feedUiState.value)
    }

    @Test
    fun oneBookmark_showsInFeed() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }

        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)

        val item = viewModel.feedUiState.value
        assertIs<Success>(item)
        assertEquals(1, item.feed.size)
    }

    @Test
    fun oneBookmark_whenRemoving_removesFromFeed() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }

        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)

        viewModel.removeFromSavedResources(newsResourcesTestData[0].id)

        val item = viewModel.feedUiState.value
        assertIs<Success>(item)
        assertEquals(0, item.feed.size)
        assertTrue(viewModel.shouldDisplayUndoBookmark)
    }

    @Test
    fun feedUiState_resourceIsViewed_setResourcesViewed() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }

        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)
        val itemBeforeViewed = viewModel.feedUiState.value
        assertIs<Success>(itemBeforeViewed)
        assertFalse(itemBeforeViewed.feed.first().hasBeenViewed)

        viewModel.setNewsResourceViewed(newsResourcesTestData[0].id, true)

        val item = viewModel.feedUiState.value
        assertIs<Success>(item)
        assertTrue(item.feed.first().hasBeenViewed)
    }

    @Test
    fun feedUiState_undoneBookmarkRemoval_bookmarkIsRestored() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }

        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)
        viewModel.removeFromSavedResources(newsResourcesTestData[0].id)
        assertTrue(viewModel.shouldDisplayUndoBookmark)
        val itemBeforeUndo = viewModel.feedUiState.value
        assertIs<Success>(itemBeforeUndo)
        assertEquals(0, itemBeforeUndo.feed.size)

        viewModel.undoBookmarkRemoval()

        assertFalse(viewModel.shouldDisplayUndoBookmark)
        val item = viewModel.feedUiState.value
        assertIs<Success>(item)
        assertEquals(1, item.feed.size)
    }

    @Test
    fun clearingUndoStateDropsTheRestoreOffer() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }

        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)
        viewModel.removeFromSavedResources(newsResourcesTestData[0].id)

        viewModel.clearUndoState()

        assertFalse(viewModel.shouldDisplayUndoBookmark)
        val item = viewModel.feedUiState.value
        assertIs<Success>(item)
        assertEquals(0, item.feed.size)
    }
}
