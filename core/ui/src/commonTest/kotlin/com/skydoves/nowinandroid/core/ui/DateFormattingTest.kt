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

package com.skydoves.nowinandroid.core.ui

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * `java.time`'s localized MEDIUM date style is JVM only, so the replacement format has to produce
 * the same shape the Android app showed: "Oct 6, 2022", with no leading zero on the day.
 */
class DateFormattingTest {

    @Test
    fun singleDigitDayIsNotPadded() {
        val date = Instant.parse("2022-10-06T23:00:00Z")
            .toLocalDateTime(TimeZone.UTC)
            .date
        assertEquals("Oct 6, 2022", date.format(MediumDateFormat))
    }

    @Test
    fun doubleDigitDayIsRendered() {
        val date = Instant.parse("2023-01-25T09:30:00Z")
            .toLocalDateTime(TimeZone.UTC)
            .date
        assertEquals("Jan 25, 2023", date.format(MediumDateFormat))
    }
}
