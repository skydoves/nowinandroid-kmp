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

package com.skydoves.nowinandroid.core.network.di

import com.skydoves.nowinandroid.core.network.NiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.demo.DemoNiaNetworkDataSource
import com.skydoves.nowinandroid.core.network.httpClientEngineFactory
import com.skydoves.nowinandroid.core.network.ktor.KtorNiaNetwork
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The Android original selected between the demo and the real backend with a Gradle product
 * flavour. Kotlin Multiplatform modules have no flavours, so the choice is a single constant: the
 * upstream repository also ships without a usable `BACKEND_URL`, and the bundled demo data is what
 * `demoDebug` builds actually run against.
 */
object NiaBackend {
    /** Set this to your backend's base URL to switch [NiaNetworkDataSource] over to Ktor. */
    const val BASE_URL: String = ""
}

@BindingContainer
@ContributesTo(AppScope::class)
object NetworkBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesHttpClient(json: Json): HttpClient = HttpClient(httpClientEngineFactory()) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.INFO }
        if (NiaBackend.BASE_URL.isNotEmpty()) {
            defaultRequest { url(NiaBackend.BASE_URL) }
        }
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesNetworkDataSource(demo: DemoNiaNetworkDataSource, ktor: KtorNiaNetwork): NiaNetworkDataSource =
        if (NiaBackend.BASE_URL.isEmpty()) demo else ktor
}
