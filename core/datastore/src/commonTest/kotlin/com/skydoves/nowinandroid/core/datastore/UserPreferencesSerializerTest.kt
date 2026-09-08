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

import androidx.datastore.core.CorruptionException
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import kotlinx.coroutines.test.runTest
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The Android original serialised these preferences with protobuf. This checks the
 * kotlinx.serialization replacement round-trips, defaults sanely, and reports corruption rather
 * than crashing.
 */
class UserPreferencesSerializerTest {

    private val subject = UserPreferencesSerializer

    @Test
    fun defaultUserPreferencesIsEmpty() {
        assertEquals(
            UserPreferences(
                followedTopicIds = emptySet(),
                bookmarkedNewsResourceIds = emptySet(),
                viewedNewsResourceIds = emptySet(),
                themeBrand = ThemeBrand.DEFAULT,
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                shouldHideOnboarding = false,
                topicChangeListVersion = 0,
                newsResourceChangeListVersion = 0,
            ),
            subject.defaultValue,
        )
    }

    @Test
    fun writingAndReadingUserPreferencesOutputsCorrectValue() = runTest {
        val expected = UserPreferences(
            followedTopicIds = setOf("1", "2"),
            bookmarkedNewsResourceIds = setOf("3"),
            viewedNewsResourceIds = setOf("3", "4"),
            themeBrand = ThemeBrand.ANDROID,
            darkThemeConfig = DarkThemeConfig.DARK,
            useDynamicColor = true,
            shouldHideOnboarding = true,
            topicChangeListVersion = 5,
            newsResourceChangeListVersion = 9,
        )

        val buffer = Buffer()
        subject.writeTo(expected, buffer)

        assertEquals(expected, subject.readFrom(buffer))
    }

    @Test
    fun readingInvalidUserPreferencesThrowsCorruptionException() = runTest {
        assertFailsWith<CorruptionException> {
            subject.readFrom(Buffer().apply { writeUtf8("not json") })
        }
    }

    @Test
    fun unknownFieldsAreIgnoredSoOlderInstallsStillLoad() = runTest {
        val json = """{"followedTopicIds":["1"],"someFieldFromTheFuture":42}"""

        val read = subject.readFrom(Buffer().apply { writeUtf8(json) })

        assertEquals(setOf("1"), read.followedTopicIds)
        assertEquals(ThemeBrand.DEFAULT, read.themeBrand)
    }
}
