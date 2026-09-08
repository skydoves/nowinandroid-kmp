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

package com.skydoves.nowinandroid.core.network.demo

import com.skydoves.nowinandroid.core.common.network.IoDispatcher
import com.skydoves.nowinandroid.core.network.NiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.generated.resources.Res
import com.skydoves.nowinandroid.core.network.model.NetworkChangeList
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.sandwich.ApiResponse
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * [NiaNetworkDataSource] implementation that provides static news resources to aid development.
 *
 * The Android original read these from `assets/` through an `AssetManager`. Compose Resources is
 * the multiplatform equivalent: the same two JSON files ship inside the app on every target.
 */
@Inject
@SingleIn(AppScope::class)
class DemoNiaNetworkDataSource(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
) : NiaNetworkDataSource {

    override suspend fun getTopics(ids: List<String>?): ApiResponse<List<NetworkTopic>> = readAsset(TOPICS_ASSET)

    override suspend fun getNewsResources(ids: List<String>?): ApiResponse<List<NetworkNewsResource>> =
        readAsset(NEWS_ASSET)

    override suspend fun getTopicChangeList(after: Int?): ApiResponse<List<NetworkChangeList>> =
        getTopics().mapToChangeList(NetworkTopic::id)

    override suspend fun getNewsResourceChangeList(after: Int?): ApiResponse<List<NetworkChangeList>> =
        getNewsResources().mapToChangeList(NetworkNewsResource::id)

    @OptIn(ExperimentalResourceApi::class)
    private suspend inline fun <reified T> readAsset(fileName: String): ApiResponse<List<T>> =
        withContext(ioDispatcher) {
            try {
                ApiResponse.Success(
                    networkJson.decodeFromString<List<T>>(
                        Res.readBytes("files/$fileName").decodeToString(),
                    ),
                )
            } catch (throwable: Throwable) {
                ApiResponse.exception(throwable)
            }
        }

    private companion object {
        const val NEWS_ASSET = "news.json"
        const val TOPICS_ASSET = "topics.json"
    }
}

/**
 * Converts a list of [T] to change list of all the items in it where [idGetter] defines the
 * [NetworkChangeList.id]
 */
private fun <T> ApiResponse<List<T>>.mapToChangeList(idGetter: (T) -> String): ApiResponse<List<NetworkChangeList>> =
    when (this) {
        is ApiResponse.Success -> ApiResponse.Success(
            data.mapIndexed { index, item ->
                NetworkChangeList(
                    id = idGetter(item),
                    changeListVersion = index,
                    isDelete = false,
                )
            },
        )

        // `Failure.Error` and `Failure.Exception` are both `Failure<Nothing>`, so they widen to any
        // `ApiResponse<T>`; the `Failure<T>` interface itself is invariant and would not.
        is ApiResponse.Failure.Error -> this
        is ApiResponse.Failure.Exception -> this
    }
