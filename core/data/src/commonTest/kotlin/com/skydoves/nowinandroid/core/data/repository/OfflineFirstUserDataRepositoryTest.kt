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

package com.skydoves.nowinandroid.core.data.repository

import com.skydoves.nowinandroid.core.analytics.NoOpAnalyticsHelper
import com.skydoves.nowinandroid.core.datastore.NiaPreferencesDataSource
import com.skydoves.nowinandroid.core.datastore.UserPreferences
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import com.skydoves.nowinandroid.core.model.data.UserData
import com.skydoves.nowinandroid.core.testing.util.InMemoryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineFirstUserDataRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstUserDataRepository
    private lateinit var niaPreferencesDataSource: NiaPreferencesDataSource
    private val analyticsHelper = NoOpAnalyticsHelper()

    @BeforeTest
    fun setup() {
        niaPreferencesDataSource = NiaPreferencesDataSource(InMemoryDataStore(UserPreferences()))
        subject = OfflineFirstUserDataRepository(niaPreferencesDataSource, analyticsHelper)
    }

    @Test
    fun defaultUserDataIsCorrect() = testScope.runTest {
        assertEquals(
            UserData(
                bookmarkedNewsResources = emptySet(),
                viewedNewsResources = emptySet(),
                followedTopics = emptySet(),
                themeBrand = ThemeBrand.DEFAULT,
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                shouldHideOnboarding = false,
            ),
            subject.userData.first(),
        )
    }

    @Test
    fun toggleFollowedTopicsDelegatesToNiaPreferences() = testScope.runTest {
        subject.setTopicIdFollowed(followedTopicId = "0", followed = true)
        assertEquals(setOf("0"), subject.userData.map { it.followedTopics }.first())

        subject.setTopicIdFollowed(followedTopicId = "1", followed = true)
        assertEquals(setOf("0", "1"), subject.userData.map { it.followedTopics }.first())

        assertEquals(
            niaPreferencesDataSource.userData.map { it.followedTopics }.first(),
            subject.userData.map { it.followedTopics }.first(),
        )
    }

    @Test
    fun setFollowedTopicsDelegatesToNiaPreferences() = testScope.runTest {
        subject.setFollowedTopicIds(followedTopicIds = setOf("1", "2"))

        assertEquals(setOf("1", "2"), subject.userData.map { it.followedTopics }.first())
        assertEquals(
            niaPreferencesDataSource.userData.map { it.followedTopics }.first(),
            subject.userData.map { it.followedTopics }.first(),
        )
    }

    @Test
    fun bookmarkNewsResourceDelegatesToNiaPreferences() = testScope.runTest {
        subject.setNewsResourceBookmarked(newsResourceId = "0", bookmarked = true)
        assertEquals(setOf("0"), subject.userData.map { it.bookmarkedNewsResources }.first())

        subject.setNewsResourceBookmarked(newsResourceId = "1", bookmarked = true)
        assertEquals(setOf("0", "1"), subject.userData.map { it.bookmarkedNewsResources }.first())

        subject.setNewsResourceBookmarked(newsResourceId = "0", bookmarked = false)
        assertEquals(setOf("1"), subject.userData.map { it.bookmarkedNewsResources }.first())
    }

    @Test
    fun updateViewedNewsResourcesDelegatesToNiaPreferences() = testScope.runTest {
        subject.setNewsResourceViewed(newsResourceId = "0", viewed = true)
        assertEquals(setOf("0"), subject.userData.map { it.viewedNewsResources }.first())

        subject.setNewsResourceViewed(newsResourceId = "1", viewed = true)
        assertEquals(setOf("0", "1"), subject.userData.map { it.viewedNewsResources }.first())
    }

    @Test
    fun setThemeBrandDelegatesToNiaPreferences() = testScope.runTest {
        subject.setThemeBrand(ThemeBrand.ANDROID)

        assertEquals(ThemeBrand.ANDROID, subject.userData.map { it.themeBrand }.first())
        assertEquals(
            ThemeBrand.ANDROID,
            niaPreferencesDataSource.userData.map { it.themeBrand }.first(),
        )
    }

    @Test
    fun setDynamicColorDelegatesToNiaPreferences() = testScope.runTest {
        subject.setDynamicColorPreference(true)

        assertTrue(subject.userData.map { it.useDynamicColor }.first())
        assertTrue(niaPreferencesDataSource.userData.map { it.useDynamicColor }.first())
    }

    @Test
    fun setDarkThemeConfigDelegatesToNiaPreferences() = testScope.runTest {
        subject.setDarkThemeConfig(DarkThemeConfig.DARK)

        assertEquals(DarkThemeConfig.DARK, subject.userData.map { it.darkThemeConfig }.first())
        assertEquals(
            DarkThemeConfig.DARK,
            niaPreferencesDataSource.userData.map { it.darkThemeConfig }.first(),
        )
    }

    @Test
    fun setShouldHideOnboardingDelegatesToNiaPreferences() = testScope.runTest {
        subject.setShouldHideOnboarding(true)
        assertTrue(subject.userData.map { it.shouldHideOnboarding }.first())

        subject.setShouldHideOnboarding(false)
        assertFalse(subject.userData.map { it.shouldHideOnboarding }.first())
    }
}
