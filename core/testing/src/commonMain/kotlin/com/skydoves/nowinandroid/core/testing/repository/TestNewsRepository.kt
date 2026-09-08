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

import com.skydoves.nowinandroid.core.data.Synchronizer
import com.skydoves.nowinandroid.core.data.repository.NewsRepository
import com.skydoves.nowinandroid.core.data.repository.NewsResourceQuery
import com.skydoves.nowinandroid.core.model.data.NewsResource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class TestNewsRepository : NewsRepository {

    private val newsResourcesFlow: MutableSharedFlow<List<NewsResource>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getNewsResources(query: NewsResourceQuery): Flow<List<NewsResource>> =
        newsResourcesFlow.map { newsResources ->
            var result = newsResources
            query.filterTopicIds?.let { filterTopicIds ->
                result = newsResources.filter {
                    it.topics.map(com.skydoves.nowinandroid.core.model.data.Topic::id)
                        .intersect(filterTopicIds)
                        .isNotEmpty()
                }
            }
            query.filterNewsIds?.let { filterNewsIds ->
                result = result.filter { it.id in filterNewsIds }
            }
            result
        }

    /**
     * A test-only API to allow controlling the list of news resources from tests.
     */
    fun sendNewsResources(newsResources: List<NewsResource>) {
        newsResourcesFlow.tryEmit(newsResources)
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
}
