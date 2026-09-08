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

package com.skydoves.nowinandroid.core.data.testdoubles

import com.skydoves.nowinandroid.core.network.NiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.demo.DemoNiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.model.NetworkChangeList
import com.skydoves.nowinandroid.core.network.model.NetworkNewsResource
import com.skydoves.nowinandroid.core.network.model.NetworkTopic
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

enum class CollectionType {
    Topics,
    NewsResources,
}

/**
 * Test double for [NiaNetworkDataSource], backed by the same bundled demo JSON the app ships, so
 * the sync tests run against the real 19 topics and 311 news resources rather than a toy fixture.
 *
 * It lives in `desktopTest` because reading it goes through Compose Resources, and only the JVM
 * test runtime can reach another module's bundled resources: an Android *host* test has no
 * `Context`, and a Kotlin/Native test binary is not given the dependency's resource bundle. The
 * repository logic these tests cover is platform independent, so running it on one JVM is enough;
 * the same path is exercised on Android and iOS by the app itself.
 */
class TestNiaNetworkDataSource : NiaNetworkDataSource {

    private val source = DemoNiaNetworkDataSource(
        UnconfinedTestDispatcher(),
        Json { ignoreUnknownKeys = true },
    )

    private val allTopics: List<NetworkTopic> by lazy {
        runBlocking { source.getTopics().getOrThrow() }
    }

    private val allNewsResources: List<NetworkNewsResource> by lazy {
        runBlocking { source.getNewsResources().getOrThrow() }
    }

    private val changeLists: MutableMap<CollectionType, List<NetworkChangeList>> by lazy {
        mutableMapOf(
            CollectionType.Topics to allTopics.mapToChangeList(NetworkTopic::id),
            CollectionType.NewsResources to
                allNewsResources.mapToChangeList(NetworkNewsResource::id),
        )
    }

    override suspend fun getTopics(ids: List<String>?): ApiResponse<List<NetworkTopic>> =
        ApiResponse.Success(allTopics.matchIds(ids, NetworkTopic::id))

    override suspend fun getNewsResources(ids: List<String>?): ApiResponse<List<NetworkNewsResource>> =
        ApiResponse.Success(allNewsResources.matchIds(ids, NetworkNewsResource::id))

    override suspend fun getTopicChangeList(after: Int?): ApiResponse<List<NetworkChangeList>> =
        ApiResponse.Success(changeLists.getValue(CollectionType.Topics).after(after))

    override suspend fun getNewsResourceChangeList(after: Int?): ApiResponse<List<NetworkChangeList>> =
        ApiResponse.Success(changeLists.getValue(CollectionType.NewsResources).after(after))

    fun latestChangeListVersion(collectionType: CollectionType) =
        changeLists.getValue(collectionType).last().changeListVersion

    fun changeListsAfter(collectionType: CollectionType, version: Int) =
        changeLists.getValue(collectionType).after(version)

    /**
     * Edits the change list for the backing [collectionType] for the given [id] mimicking
     * the server's change list registry
     */
    fun editCollection(collectionType: CollectionType, id: String, isDelete: Boolean) {
        val changeList = changeLists.getValue(collectionType)
        val latestVersion = changeList.lastOrNull()?.changeListVersion ?: 0
        val change = NetworkChangeList(
            id = id,
            isDelete = isDelete,
            changeListVersion = latestVersion + 1,
        )
        changeLists[collectionType] = changeList.filterNot { it.id == id } + change
    }
}

fun List<NetworkChangeList>.after(version: Int?): List<NetworkChangeList> = when (version) {
    null -> this
    else -> filter { it.changeListVersion > version }
}

/**
 * Return items from [this] whose id defined by [idGetter] is in [ids] if [ids] is not null
 */
private fun <T> List<T>.matchIds(ids: List<String>?, idGetter: (T) -> String) = when (ids) {
    null -> this
    else -> ids.toSet().let { idSet -> filter { idGetter(it) in idSet } }
}

/**
 * Maps items to a change list where the change list version is denoted by the index of each item.
 */
private fun <T> List<T>.mapToChangeList(idGetter: (T) -> String) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter(item),
        changeListVersion = index + 1,
        isDelete = false,
    )
}
