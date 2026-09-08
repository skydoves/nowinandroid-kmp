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

import com.skydoves.nowinandroid.core.network.ktor.KtorNiaNetwork
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.getOrThrow
import com.skydoves.sandwich.message
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The Ktor + Sandwich replacement for Retrofit. What matters is that an HTTP error and a transport
 * failure come back as two distinct `ApiResponse` branches rather than one thrown exception.
 */
class KtorNiaNetworkTest {

    private fun network(handler: MockEngine) = KtorNiaNetwork(
        HttpClient(handler) {
            expectSuccess = false
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        },
    )

    private fun jsonEngine(body: String) = MockEngine {
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
        )
    }

    @Test
    fun topicsAreUnwrappedFromTheDataEnvelope() = runTest {
        val subject = network(
            jsonEngine("""{"data":[{"id":"1","name":"Headlines"},{"id":"2","name":"UI"}]}"""),
        )

        val topics = subject.getTopics().getOrThrow()

        assertEquals(listOf("1", "2"), topics.map { it.id })
        assertEquals(listOf("Headlines", "UI"), topics.map { it.name })
    }

    @Test
    fun newsResourcesAreUnwrappedFromTheDataEnvelope() = runTest {
        val subject = network(
            jsonEngine(
                """{"data":[{"id":"n1","title":"t","content":"c","url":"u",
                |"headerImageUrl":"h","publishDate":"2022-10-04T23:00:00.000Z",
                |"type":"Article","topics":["1"]}]}
                """.trimMargin(),
            ),
        )

        val news = subject.getNewsResources().getOrThrow()

        assertEquals(listOf("n1"), news.map { it.id })
        assertEquals(listOf(listOf("1")), news.map { it.topics })
    }

    @Test
    fun changeListsAreNotEnveloped() = runTest {
        val subject = network(
            jsonEngine("""[{"id":"1","changeListVersion":3,"isDelete":false}]"""),
        )

        val changeList = subject.getTopicChangeList().getOrThrow()

        assertEquals(1, changeList.size)
        assertEquals(3, changeList.single().changeListVersion)
    }

    @Test
    fun idsAreSentAsRepeatedQueryParameters() = runTest {
        var requestedUrl = ""
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = """{"data":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }

        network(engine).getTopics(ids = listOf("1", "2")).getOrThrow()

        assertContains(requestedUrl, "id=1")
        assertContains(requestedUrl, "id=2")
    }

    @Test
    fun anHttpErrorIsAFailureError() = runTest {
        val subject = network(MockEngine { respondError(HttpStatusCode.InternalServerError) })

        val response = subject.getTopics()

        val failure = assertIs<ApiResponse.Failure.Error>(response)
        assertTrue(failure.message().isNotEmpty())
    }

    @Test
    fun aTransportFailureIsAFailureException() = runTest {
        val subject = network(MockEngine { throw kotlinx.io.IOException("no network") })

        val response = subject.getNewsResources()

        val failure = assertIs<ApiResponse.Failure.Exception>(response)
        assertEquals("no network", failure.message)
    }
}
