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

package com.skydoves.nowinandroid.core.network.ktor

import com.skydoves.nowinandroid.core.network.NiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.model.NetworkChangeList
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ktor.getApiResponse
import com.skydoves.sandwich.mapSuccess
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

/**
 * Wrapper for data provided from the NIA backend.
 */
@Serializable
internal data class NetworkResponse<T>(val data: T)

/**
 * Ktor backed [NiaNetworkDataSource]. Replaces the Android original's Retrofit + OkHttp stack,
 * which has no Kotlin/Native transport.
 */
@Inject
@SingleIn(AppScope::class)
class KtorNiaNetwork(private val httpClient: HttpClient) : NiaNetworkDataSource {

    override suspend fun getTopics(ids: List<String>?): ApiResponse<List<NetworkTopic>> =
        httpClient.getApiResponse<NetworkResponse<List<NetworkTopic>>>("topics") {
            ids?.forEach { parameter("id", it) }
        }.mapSuccess { data }

    override suspend fun getNewsResources(ids: List<String>?): ApiResponse<List<NetworkNewsResource>> =
        httpClient.getApiResponse<NetworkResponse<List<NetworkNewsResource>>>("newsresources") {
            ids?.forEach { parameter("id", it) }
        }.mapSuccess { data }

    override suspend fun getTopicChangeList(after: Int?): ApiResponse<List<NetworkChangeList>> =
        httpClient.getApiResponse("changelists/topics") {
            after?.let { parameter("after", it) }
        }

    override suspend fun getNewsResourceChangeList(after: Int?): ApiResponse<List<NetworkChangeList>> =
        httpClient.getApiResponse("changelists/newsresources") {
            after?.let { parameter("after", it) }
        }
}
