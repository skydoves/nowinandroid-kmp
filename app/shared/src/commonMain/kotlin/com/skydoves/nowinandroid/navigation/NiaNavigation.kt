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

package com.skydoves.nowinandroid.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.skydoves.nowinandroid.core.designsystem.icon.NiaIcons
import com.skydoves.nowinandroid.feature.bookmarks.api.generated.resources.feature_bookmarks_api_title
import com.skydoves.nowinandroid.feature.bookmarks.api.navigation.BookmarksNavKey
import com.skydoves.nowinandroid.feature.foryou.api.generated.resources.feature_foryou_api_title
import com.skydoves.nowinandroid.feature.foryou.api.navigation.ForYouNavKey
import com.skydoves.nowinandroid.feature.interests.api.navigation.InterestsNavKey
import com.skydoves.nowinandroid.feature.search.api.generated.resources.feature_search_api_interests
import com.skydoves.nowinandroid.feature.search.api.navigation.SearchNavKey
import com.skydoves.nowinandroid.feature.topic.api.navigation.TopicNavKey
import com.skydoves.nowinandroid.generated.resources.Res
import com.skydoves.nowinandroid.generated.resources.app_name
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.StringResource
import com.skydoves.nowinandroid.feature.bookmarks.api.generated.resources.Res as BookmarksApiRes
import com.skydoves.nowinandroid.feature.foryou.api.generated.resources.Res as ForYouApiRes
import com.skydoves.nowinandroid.feature.search.api.generated.resources.Res as SearchApiRes

/**
 * Type for the top level navigation items in the application. Contains UI information about the
 * current route that is used in the top app bar and common navigation UI.
 *
 * @param selectedIcon The icon to be displayed in the navigation UI when this destination is
 * selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this destination is
 * not selected.
 * @param iconText Text that to be displayed in the navigation UI.
 * @param titleText Text that is displayed on the top app bar.
 */
data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: StringResource,
    val titleText: StringResource,
)

val FOR_YOU = TopLevelNavItem(
    selectedIcon = NiaIcons.Upcoming,
    unselectedIcon = NiaIcons.UpcomingBorder,
    iconText = ForYouApiRes.string.feature_foryou_api_title,
    titleText = Res.string.app_name,
)

val BOOKMARKS = TopLevelNavItem(
    selectedIcon = NiaIcons.Bookmarks,
    unselectedIcon = NiaIcons.BookmarksBorder,
    iconText = BookmarksApiRes.string.feature_bookmarks_api_title,
    titleText = BookmarksApiRes.string.feature_bookmarks_api_title,
)

val INTERESTS = TopLevelNavItem(
    selectedIcon = NiaIcons.Grid3x3,
    unselectedIcon = NiaIcons.Grid3x3,
    iconText = SearchApiRes.string.feature_search_api_interests,
    titleText = SearchApiRes.string.feature_search_api_interests,
)

val TOP_LEVEL_NAV_ITEMS: Map<NavKey, TopLevelNavItem> = mapOf(
    ForYouNavKey to FOR_YOU,
    BookmarksNavKey to BOOKMARKS,
    InterestsNavKey(null) to INTERESTS,
)

/**
 * Navigation 3 serialises the back stack to survive process death, and open polymorphism needs
 * every [NavKey] subclass registered up front.
 */
val NiaSavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ForYouNavKey::class)
            subclass(BookmarksNavKey::class)
            subclass(InterestsNavKey::class)
            subclass(TopicNavKey::class)
            subclass(SearchNavKey::class)
        }
    }
}
