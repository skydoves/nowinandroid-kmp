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

import com.skydoves.nowinandroid.core.database.dao.NewsResourceDao
import com.skydoves.nowinandroid.core.database.model.NewsResourceEntity
import com.skydoves.nowinandroid.core.database.model.NewsResourceTopicCrossRef
import com.skydoves.nowinandroid.core.database.model.PopulatedNewsResource
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

val filteredInterestsIds = setOf("1")
val nonPresentInterestsIds = setOf("2")

/**
 * Test double for [NewsResourceDao]
 */
class TestNewsResourceDao : NewsResourceDao {

    private val entitiesStateFlow = MutableStateFlow(emptyList<NewsResourceEntity>())

    internal var topicCrossReferences: List<NewsResourceTopicCrossRef> = emptyList()

    override fun getNewsResources(
        useFilterTopicIds: Boolean,
        filterTopicIds: Set<String>,
        useFilterNewsIds: Boolean,
        filterNewsIds: Set<String>,
    ): Flow<List<PopulatedNewsResource>> = entitiesStateFlow
        .map { entities -> entities.map { it.asPopulatedNewsResource(topicCrossReferences) } }
        .map { resources ->
            resources.filtered(useFilterTopicIds, filterTopicIds, useFilterNewsIds, filterNewsIds)
        }

    override fun getNewsResourceIds(
        useFilterTopicIds: Boolean,
        filterTopicIds: Set<String>,
        useFilterNewsIds: Boolean,
        filterNewsIds: Set<String>,
    ): Flow<List<String>> = getNewsResources(
        useFilterTopicIds = useFilterTopicIds,
        filterTopicIds = filterTopicIds,
        useFilterNewsIds = useFilterNewsIds,
        filterNewsIds = filterNewsIds,
    ).map { resources -> resources.map { it.entity.id } }

    override suspend fun upsertNewsResources(newsResourceEntities: List<NewsResourceEntity>) {
        entitiesStateFlow.update { oldValues ->
            // New values come first so they overwrite old values
            (newsResourceEntities + oldValues)
                .distinctBy(NewsResourceEntity::id)
                .sortedWith(compareBy(NewsResourceEntity::publishDate).reversed())
        }
    }

    override suspend fun insertOrIgnoreTopicCrossRefEntities(
        newsResourceTopicCrossReferences: List<NewsResourceTopicCrossRef>,
    ) {
        // Keep old values over new ones
        topicCrossReferences = (topicCrossReferences + newsResourceTopicCrossReferences)
            .distinctBy { it.newsResourceId to it.topicId }
    }

    override suspend fun deleteNewsResources(ids: List<String>) {
        val idSet = ids.toSet()
        entitiesStateFlow.update { entities -> entities.filterNot { it.id in idSet } }
    }
}

private fun List<PopulatedNewsResource>.filtered(
    useFilterTopicIds: Boolean,
    filterTopicIds: Set<String>,
    useFilterNewsIds: Boolean,
    filterNewsIds: Set<String>,
): List<PopulatedNewsResource> {
    var result = this
    if (useFilterTopicIds) {
        result = result.filter { resource -> resource.topics.any { it.id in filterTopicIds } }
    }
    if (useFilterNewsIds) {
        result = result.filter { resource -> resource.entity.id in filterNewsIds }
    }
    return result
}

private fun NewsResourceEntity.asPopulatedNewsResource(topicCrossReferences: List<NewsResourceTopicCrossRef>) =
    PopulatedNewsResource(
        entity = this,
        topics = topicCrossReferences
            .filter { it.newsResourceId == id }
            .map { crossRef ->
                TopicEntity(
                    id = crossRef.topicId,
                    name = "name",
                    shortDescription = "short description",
                    longDescription = "long description",
                    url = "URL",
                    imageUrl = "image URL",
                )
            },
    )
