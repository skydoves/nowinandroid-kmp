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

import com.skydoves.nowinandroid.core.data.model.RecentSearchQuery
import com.skydoves.nowinandroid.core.data.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestRecentSearchRepository : RecentSearchRepository {

    private val cachedRecentSearches = MutableStateFlow(emptyList<RecentSearchQuery>())

    override fun getRecentSearchQueries(limit: Int): Flow<List<RecentSearchQuery>> =
        cachedRecentSearches.map { queries ->
            queries.sortedByDescending { it.queriedDate }.take(limit)
        }

    override suspend fun insertOrReplaceRecentSearch(searchQuery: String) {
        cachedRecentSearches.value = cachedRecentSearches.value
            .filterNot { it.query == searchQuery } + RecentSearchQuery(searchQuery)
    }

    override suspend fun clearRecentSearches() {
        cachedRecentSearches.value = emptyList()
    }
}
