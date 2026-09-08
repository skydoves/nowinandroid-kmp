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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavKey
import com.skydoves.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.skydoves.nowinandroid.core.data.util.NetworkMonitor
import com.skydoves.nowinandroid.core.data.util.TimeZoneMonitor
import com.skydoves.nowinandroid.core.navigation.NavigationState
import com.skydoves.nowinandroid.core.navigation.rememberNavigationState
import com.skydoves.nowinandroid.core.ui.TrackDisposableJank
import com.skydoves.nowinandroid.feature.bookmarks.api.navigation.BookmarksNavKey
import com.skydoves.nowinandroid.feature.foryou.api.navigation.ForYouNavKey
import com.skydoves.nowinandroid.navigation.NiaSavedStateConfiguration
import com.skydoves.nowinandroid.navigation.TOP_LEVEL_NAV_ITEMS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

@Composable
fun rememberNiaAppState(
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): NiaAppState {
    val navigationState = rememberNavigationState(
        configuration = NiaSavedStateConfiguration,
        startKey = ForYouNavKey,
        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
    )

    NavigationTrackingSideEffect(navigationState)

    return remember(
        navigationState,
        coroutineScope,
        networkMonitor,
        userNewsResourceRepository,
        timeZoneMonitor,
    ) {
        NiaAppState(
            navigationState = navigationState,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            userNewsResourceRepository = userNewsResourceRepository,
            timeZoneMonitor = timeZoneMonitor,
        )
    }
}

@Stable
class NiaAppState(
    val navigationState: NavigationState,
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
) {
    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * The top level nav keys that have unread news resources.
     */
    val topLevelNavKeysWithUnreadResources: StateFlow<Set<NavKey>> =
        userNewsResourceRepository.observeAllForFollowedTopics()
            .combine(
                userNewsResourceRepository.observeAllBookmarked(),
            ) { forYouNewsResources, bookmarkedNewsResources ->
                setOfNotNull(
                    ForYouNavKey.takeIf { forYouNewsResources.any { !it.hasBeenViewed } },
                    BookmarksNavKey.takeIf { bookmarkedNewsResources.any { !it.hasBeenViewed } },
                )
            }
            .stateIn(
                coroutineScope,
                SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet(),
            )

    val currentTimeZone = timeZoneMonitor.currentTimeZone
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            TimeZone.currentSystemDefault(),
        )
}

/**
 * Stores information about navigation events to be used with JankStats
 */
@Composable
private fun NavigationTrackingSideEffect(navigationState: NavigationState) {
    TrackDisposableJank(navigationState.currentKey) { metricsHolder ->
        metricsHolder.putState("Navigation", navigationState.currentKey.toString())
        onDispose {}
    }
}
