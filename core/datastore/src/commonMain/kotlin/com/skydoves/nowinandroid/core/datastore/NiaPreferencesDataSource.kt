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

package com.skydoves.nowinandroid.core.datastore

import androidx.datastore.core.DataStore
import com.skydoves.nowinandroid.core.common.log.NiaLogger
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import com.skydoves.nowinandroid.core.model.data.UserData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okio.IOException

private const val TAG = "NiaPreferences"

@Inject
@SingleIn(AppScope::class)
class NiaPreferencesDataSource(private val userPreferences: DataStore<UserPreferences>) {
    val userData: Flow<UserData> = userPreferences.data
        .map {
            UserData(
                bookmarkedNewsResources = it.bookmarkedNewsResourceIds,
                viewedNewsResources = it.viewedNewsResourceIds,
                followedTopics = it.followedTopicIds,
                themeBrand = it.themeBrand,
                darkThemeConfig = it.darkThemeConfig,
                useDynamicColor = it.useDynamicColor,
                shouldHideOnboarding = it.shouldHideOnboarding,
            )
        }

    suspend fun setFollowedTopicIds(topicIds: Set<String>) {
        updateData { it.copy(followedTopicIds = topicIds).withOnboardingReset() }
    }

    suspend fun setTopicIdFollowed(topicId: String, followed: Boolean) {
        updateData { current ->
            val followedTopicIds = if (followed) {
                current.followedTopicIds + topicId
            } else {
                current.followedTopicIds - topicId
            }
            current.copy(followedTopicIds = followedTopicIds).withOnboardingReset()
        }
    }

    suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        updateData { it.copy(themeBrand = themeBrand) }
    }

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        updateData { it.copy(useDynamicColor = useDynamicColor) }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        updateData { it.copy(darkThemeConfig = darkThemeConfig) }
    }

    suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        updateData { current ->
            val bookmarks = if (bookmarked) {
                current.bookmarkedNewsResourceIds + newsResourceId
            } else {
                current.bookmarkedNewsResourceIds - newsResourceId
            }
            current.copy(bookmarkedNewsResourceIds = bookmarks)
        }
    }

    suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) =
        setNewsResourcesViewed(listOf(newsResourceId), viewed)

    suspend fun setNewsResourcesViewed(newsResourceIds: List<String>, viewed: Boolean) {
        updateData { current ->
            val viewedIds = if (viewed) {
                current.viewedNewsResourceIds + newsResourceIds
            } else {
                current.viewedNewsResourceIds - newsResourceIds.toSet()
            }
            current.copy(viewedNewsResourceIds = viewedIds)
        }
    }

    suspend fun getChangeListVersions(): ChangeListVersions = userPreferences.data
        .map {
            ChangeListVersions(
                topicVersion = it.topicChangeListVersion,
                newsResourceVersion = it.newsResourceChangeListVersion,
            )
        }
        .firstOrNull() ?: ChangeListVersions()

    /**
     * Update the [ChangeListVersions] using [update].
     */
    suspend fun updateChangeListVersion(update: ChangeListVersions.() -> ChangeListVersions) {
        updateData { current ->
            val updated = update(
                ChangeListVersions(
                    topicVersion = current.topicChangeListVersion,
                    newsResourceVersion = current.newsResourceChangeListVersion,
                ),
            )
            current.copy(
                topicChangeListVersion = updated.topicVersion,
                newsResourceChangeListVersion = updated.newsResourceVersion,
            )
        }
    }

    suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        updateData { it.copy(shouldHideOnboarding = shouldHideOnboarding) }
    }

    private suspend fun updateData(transform: (UserPreferences) -> UserPreferences) {
        try {
            userPreferences.updateData(transform)
        } catch (ioException: IOException) {
            NiaLogger.error(TAG, "Failed to update user preferences", ioException)
        }
    }
}

/**
 * Onboarding is only "done" while the user actually follows something; dropping the last topic
 * puts them back at the start, exactly as the Android original did.
 */
private fun UserPreferences.withOnboardingReset(): UserPreferences =
    if (followedTopicIds.isEmpty()) copy(shouldHideOnboarding = false) else this
