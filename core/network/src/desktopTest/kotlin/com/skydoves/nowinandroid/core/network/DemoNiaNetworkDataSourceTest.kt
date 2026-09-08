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

import com.skydoves.nowinandroid.core.network.demo.DemoNiaNetworkDataSource
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The bundled demo JSON is the app's only data source out of the box, so it is worth proving that
 * Compose Resources can actually read it off Android and that it still deserialises.
 */
class DemoNiaNetworkDataSourceTest {

    private val subject = DemoNiaNetworkDataSource(
        ioDispatcher = Dispatchers.Default,
        networkJson = Json { ignoreUnknownKeys = true },
    )

    @Test
    fun topicsAreParsed() = runTest {
        val topics = subject.getTopics().getOrThrow()
        assertEquals(19, topics.size)
        assertEquals("Headlines", topics.first().name)
    }

    @Test
    fun newsResourcesAreParsed() = runTest {
        val news = subject.getNewsResources().getOrThrow()
        assertEquals(311, news.size)
        assertTrue(news.all { it.id.isNotEmpty() })
    }

    @Test
    fun changeListsAreSynthesisedInOrder() = runTest {
        val changeList = subject.getTopicChangeList().getOrThrow()
        assertEquals(19, changeList.size)
        assertEquals(0, changeList.first().changeListVersion)
        assertEquals(18, changeList.last().changeListVersion)
        assertTrue(changeList.none { it.isDelete })
    }
}
