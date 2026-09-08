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

package com.skydoves.nowinandroid.core.domain

import com.skydoves.nowinandroid.core.testing.repository.TestRecentSearchRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRecentSearchQueriesUseCaseTest {

    private val recentSearchRepository = TestRecentSearchRepository()
    private val useCase = GetRecentSearchQueriesUseCase(recentSearchRepository)

    @Test
    fun recentSearchesAreReturnedMostRecentFirst() = runTest {
        recentSearchRepository.insertOrReplaceRecentSearch("kotlin")
        recentSearchRepository.insertOrReplaceRecentSearch("compose")

        assertEquals(listOf("compose", "kotlin"), useCase().first().map { it.query })
    }

    @Test
    fun theLimitIsHonoured() = runTest {
        repeat(5) { recentSearchRepository.insertOrReplaceRecentSearch("query $it") }

        assertEquals(3, useCase(limit = 3).first().size)
    }

    @Test
    fun noRecentSearchesReturnsAnEmptyList() = runTest {
        assertTrue(useCase().first().isEmpty())
    }
}
