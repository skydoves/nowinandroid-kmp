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

import com.skydoves.nowinandroid.core.data.repository.SearchContentsRepository
import com.skydoves.nowinandroid.core.data.repository.UserDataRepository
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.core.model.data.SearchResult
import com.skydoves.nowinandroid.core.model.data.UserData
import com.skydoves.nowinandroid.core.model.data.UserNewsResource
import com.skydoves.nowinandroid.core.model.data.UserSearchResult
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * A use case which returns the searched contents matched with the search query.
 */
@Inject
class GetSearchContentsUseCase(
    private val searchContentsRepository: SearchContentsRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(searchQuery: String): Flow<UserSearchResult> =
        searchContentsRepository.searchContents(searchQuery)
            .mapToUserSearchResult(userDataRepository.userData)
}

private fun Flow<SearchResult>.mapToUserSearchResult(userDataStream: Flow<UserData>): Flow<UserSearchResult> =
    combine(userDataStream) { searchResult, userData ->
        UserSearchResult(
            topics = searchResult.topics.map { topic ->
                FollowableTopic(
                    topic = topic,
                    isFollowed = topic.id in userData.followedTopics,
                )
            },
            newsResources = searchResult.newsResources.map { news ->
                UserNewsResource(newsResource = news, userData = userData)
            },
        )
    }
