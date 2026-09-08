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

package com.skydoves.nowinandroid.ui

import androidx.navigation3.runtime.NavBackStack
import com.skydoves.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.skydoves.nowinandroid.core.navigation.NavigationState
import com.skydoves.nowinandroid.core.navigation.Navigator
import com.skydoves.nowinandroid.core.testing.repository.TestNewsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.util.TestNetworkMonitor
import com.skydoves.nowinandroid.core.testing.util.TestTimeZoneMonitor
import com.skydoves.nowinandroid.feature.bookmarks.api.navigation.BookmarksNavKey
import com.skydoves.nowinandroid.feature.foryou.api.navigation.ForYouNavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests the parts of [NiaAppState] that do not need a composition, so they run on every target.
 * The `rememberNiaAppState` cases live in `NiaAppStateCompositionTest`.
 */
class NiaAppStateTest {

    private val networkMonitor = TestNetworkMonitor()
    private val timeZoneMonitor = TestTimeZoneMonitor()
    private val userNewsResourceRepository =
        CompositeUserNewsResourceRepository(TestNewsRepository(), TestUserDataRepository())

    private fun testNavigationState() = NavigationState(
        startKey = ForYouNavKey,
        topLevelStack = NavBackStack(ForYouNavKey),
        subStacks = mapOf(
            ForYouNavKey to NavBackStack(ForYouNavKey),
            BookmarksNavKey to NavBackStack(BookmarksNavKey),
        ),
    )

    private fun CoroutineScope.appState(navigationState: NavigationState) = NiaAppState(
        navigationState = navigationState,
        coroutineScope = this,
        networkMonitor = networkMonitor,
        userNewsResourceRepository = userNewsResourceRepository,
        timeZoneMonitor = timeZoneMonitor,
    )

    @Test
    fun currentDestinationFollowsTheNavigator() = runTest {
        val navigationState = testNavigationState()
        val navigator = Navigator(navigationState)
        val state = backgroundScope.appState(navigationState)

        assertEquals(ForYouNavKey, state.navigationState.currentTopLevelKey)
        assertEquals(ForYouNavKey, state.navigationState.currentKey)

        navigator.navigate(BookmarksNavKey)

        assertEquals(BookmarksNavKey, state.navigationState.currentTopLevelKey)
        assertEquals(BookmarksNavKey, state.navigationState.currentKey)
    }

    @Test
    fun whenNetworkMonitorIsOffline_stateIsOffline() = runTest(UnconfinedTestDispatcher()) {
        val state = backgroundScope.appState(testNavigationState())

        backgroundScope.launch { state.isOffline.collect() }
        networkMonitor.setConnected(false)

        assertTrue(state.isOffline.value)
    }

    @Test
    fun whenTimeZoneMonitorChanges_stateFollowsIt() = runTest(UnconfinedTestDispatcher()) {
        val state = backgroundScope.appState(testNavigationState())

        backgroundScope.launch { state.currentTimeZone.collect() }
        val expected = TimeZone.of("Europe/Prague")
        timeZoneMonitor.setTimeZone(expected)

        assertEquals(expected, state.currentTimeZone.value)
    }
}
