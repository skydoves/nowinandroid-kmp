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

package com.skydoves.nowinandroid.core.testing.repository

import com.skydoves.nowinandroid.core.data.repository.UserDataRepository
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import com.skydoves.nowinandroid.core.model.data.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

val emptyUserData = UserData(
    bookmarkedNewsResources = emptySet(),
    viewedNewsResources = emptySet(),
    followedTopics = emptySet(),
    themeBrand = ThemeBrand.DEFAULT,
    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    useDynamicColor = false,
    shouldHideOnboarding = false,
)

class TestUserDataRepository : UserDataRepository {

    /**
     * The backing hot flow for the list of followed topic ids for testing.
     */
    private val internalUserData = MutableSharedFlow<UserData>(replay = 1, extraBufferCapacity = 1)

    private val currentUserData get() = internalUserData.replayCache.firstOrNull() ?: emptyUserData

    override val userData: Flow<UserData> = internalUserData

    override suspend fun setFollowedTopicIds(followedTopicIds: Set<String>) {
        internalUserData.tryEmit(currentUserData.copy(followedTopics = followedTopicIds))
    }

    override suspend fun setTopicIdFollowed(followedTopicId: String, followed: Boolean) {
        val followedTopics = currentUserData.followedTopics.toMutableSet()
        if (followed) followedTopics.add(followedTopicId) else followedTopics.remove(followedTopicId)
        internalUserData.tryEmit(currentUserData.copy(followedTopics = followedTopics))
    }

    override suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        val bookmarkedNewsResources = currentUserData.bookmarkedNewsResources.toMutableSet()
        if (bookmarked) {
            bookmarkedNewsResources.add(newsResourceId)
        } else {
            bookmarkedNewsResources.remove(newsResourceId)
        }
        internalUserData.tryEmit(
            currentUserData.copy(bookmarkedNewsResources = bookmarkedNewsResources),
        )
    }

    override suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        val viewedNewsResources = currentUserData.viewedNewsResources.toMutableSet()
        if (viewed) viewedNewsResources.add(newsResourceId) else viewedNewsResources.remove(newsResourceId)
        internalUserData.tryEmit(currentUserData.copy(viewedNewsResources = viewedNewsResources))
    }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        internalUserData.tryEmit(currentUserData.copy(themeBrand = themeBrand))
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        internalUserData.tryEmit(currentUserData.copy(darkThemeConfig = darkThemeConfig))
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        internalUserData.tryEmit(currentUserData.copy(useDynamicColor = useDynamicColor))
    }

    override suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        internalUserData.tryEmit(currentUserData.copy(shouldHideOnboarding = shouldHideOnboarding))
    }

    /**
     * A test-only API to allow setting of user data directly.
     */
    fun setUserData(userData: UserData) {
        internalUserData.tryEmit(userData)
    }

    suspend fun getCurrentUserData(): UserData = userData.first()
}
