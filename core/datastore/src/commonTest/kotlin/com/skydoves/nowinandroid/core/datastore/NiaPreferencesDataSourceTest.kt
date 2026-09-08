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

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The Android original persisted these preferences as protobuf. This exercises the
 * kotlinx.serialization replacement, including the "unfollowing everything resets onboarding" rule.
 */
class NiaPreferencesDataSourceTest {

    private fun TestScope.subject(): NiaPreferencesDataSource {
        val fileSystem = FakeFileSystem()
        return NiaPreferencesDataSource(
            DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = fileSystem,
                    serializer = UserPreferencesSerializer,
                    producePath = { "/user_preferences.json".toPath() },
                ),
                scope = backgroundScope,
            ),
        )
    }

    @Test
    fun defaultUserData() = runTest {
        val userData = subject().userData.first()

        assertEquals(emptySet(), userData.followedTopics)
        assertEquals(ThemeBrand.DEFAULT, userData.themeBrand)
        assertEquals(DarkThemeConfig.FOLLOW_SYSTEM, userData.darkThemeConfig)
        assertFalse(userData.shouldHideOnboarding)
    }

    @Test
    fun followingATopicIsPersisted() = runTest {
        val subject = subject()

        subject.setTopicIdFollowed("1", true)
        subject.setTopicIdFollowed("2", true)
        subject.setTopicIdFollowed("1", false)

        assertEquals(setOf("2"), subject.userData.first().followedTopics)
    }

    @Test
    fun unfollowingTheLastTopicResetsOnboarding() = runTest {
        val subject = subject()

        subject.setTopicIdFollowed("1", true)
        subject.setShouldHideOnboarding(true)
        assertTrue(subject.userData.first().shouldHideOnboarding)

        subject.setTopicIdFollowed("1", false)

        assertFalse(subject.userData.first().shouldHideOnboarding)
    }

    @Test
    fun changeListVersionsRoundTrip() = runTest {
        val subject = subject()

        subject.updateChangeListVersion { copy(topicVersion = 7, newsResourceVersion = 11) }

        assertEquals(
            ChangeListVersions(topicVersion = 7, newsResourceVersion = 11),
            subject.getChangeListVersions(),
        )
    }

    @Test
    fun bookmarksAndViewedResourcesArePersisted() = runTest {
        val subject = subject()

        subject.setNewsResourceBookmarked("n1", true)
        subject.setNewsResourcesViewed(listOf("n1", "n2"), true)
        subject.setNewsResourceViewed("n2", false)

        val userData = subject.userData.first()
        assertEquals(setOf("n1"), userData.bookmarkedNewsResources)
        assertEquals(setOf("n1"), userData.viewedNewsResources)
    }
}
