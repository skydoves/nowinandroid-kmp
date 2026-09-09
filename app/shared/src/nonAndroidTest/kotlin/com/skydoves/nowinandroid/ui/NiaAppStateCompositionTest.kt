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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.skydoves.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.skydoves.nowinandroid.core.testing.repository.TestNewsRepository
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.util.TestNetworkMonitor
import com.skydoves.nowinandroid.core.testing.util.TestTimeZoneMonitor
import com.skydoves.nowinandroid.feature.bookmarks.api.navigation.BookmarksNavKey
import com.skydoves.nowinandroid.feature.foryou.api.navigation.ForYouNavKey
import com.skydoves.nowinandroid.feature.interests.api.navigation.InterestsNavKey
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The composition-bound half of the [NiaAppState] tests.
 *
 * The Android original needed Robolectric and a Hilt test application to get a composition. On
 * Compose Multiplatform `runComposeUiTest` provides one directly, but only off Android: an Android
 * *host* unit test still has no window to compose into. These therefore run on the desktop JVM.
 */
class NiaAppStateCompositionTest {

    private val networkMonitor = TestNetworkMonitor()
    private val timeZoneMonitor = TestTimeZoneMonitor()
    private val userNewsResourceRepository =
        CompositeUserNewsResourceRepository(TestNewsRepository(), TestUserDataRepository())

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberNiaAppStateExposesTheThreeTopLevelDestinations() = runComposeUiTest {
        lateinit var state: NiaAppState

        setContent {
            state = rememberNiaAppState(
                networkMonitor = networkMonitor,
                userNewsResourceRepository = userNewsResourceRepository,
                timeZoneMonitor = timeZoneMonitor,
            )
        }

        val topLevelKeys = state.navigationState.topLevelKeys
        assertEquals(3, topLevelKeys.size)
        assertEquals(
            setOf(ForYouNavKey, BookmarksNavKey, InterestsNavKey(null)),
            topLevelKeys,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberNiaAppStateStartsOnTheForYouDestination() = runComposeUiTest {
        lateinit var state: NiaAppState

        setContent {
            state = rememberNiaAppState(
                networkMonitor = networkMonitor,
                userNewsResourceRepository = userNewsResourceRepository,
                timeZoneMonitor = timeZoneMonitor,
            )
        }

        assertEquals(ForYouNavKey, state.navigationState.startKey)
        assertEquals(ForYouNavKey, state.navigationState.currentKey)
    }
}
