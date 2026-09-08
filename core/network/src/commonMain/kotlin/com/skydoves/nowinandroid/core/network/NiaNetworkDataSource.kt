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

package com.skydoves.nowinandroid.core.network

import com.skydoves.nowinandroid.core.network.model.NetworkChangeList
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.sandwich.ApiResponse

/**
 * Interface representing network calls to the NIA backend.
 *
 * Every call is modelled as a Sandwich [ApiResponse], so an HTTP error and a transport failure are
 * two distinct, inspectable results rather than a thrown exception the caller has to guess at.
 */
interface NiaNetworkDataSource {
    suspend fun getTopics(ids: List<String>? = null): ApiResponse<List<NetworkTopic>>

    suspend fun getNewsResources(ids: List<String>? = null): ApiResponse<List<NetworkNewsResource>>

    suspend fun getTopicChangeList(after: Int? = null): ApiResponse<List<NetworkChangeList>>

    suspend fun getNewsResourceChangeList(after: Int? = null): ApiResponse<List<NetworkChangeList>>
}
