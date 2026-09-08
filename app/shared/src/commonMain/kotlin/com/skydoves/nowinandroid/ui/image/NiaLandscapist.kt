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

package com.skydoves.nowinandroid.ui.image

import androidx.compose.runtime.Composable
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.NetworkConfig
import com.skydoves.landscapist.core.cache.DiskCache
import com.skydoves.landscapist.core.network.KtorImageFetcher
import com.skydoves.landscapist.svg.SvgImageDecoder
import io.ktor.client.HttpClient

/**
 * Builds the app's [Landscapist] instance.
 *
 * Provided through `LocalLandscapist`, which every `LandscapistImage` resolves before it falls back
 * to a default instance. That composition local is the override mechanism on every platform.
 *
 * Two things are deliberate here: the fetcher is handed the app's own Ktor client rather than
 * letting Landscapist start a second HTTP stack, and [SvgImageDecoder] wraps the raster decoder so
 * the SVG topic icons render, because Landscapist's engine has no SVG path of its own.
 */
@Composable
internal fun rememberNiaLandscapist(httpClient: HttpClient): Landscapist = rememberLandscapist(httpClient)

internal fun buildNiaLandscapist(httpClient: HttpClient, diskCache: DiskCache?): Landscapist = Landscapist.builder()
    .fetcher(KtorImageFetcher(httpClient, NetworkConfig()))
    .decoder(SvgImageDecoder())
    .apply { diskCache?.let { diskCache(it) } }
    .build()

/**
 * The disk cache differs by platform: Android needs a `Context` to find its cache directory, and
 * Landscapist's own default returns null there for exactly that reason, while iOS and desktop can
 * resolve one without help.
 */
@Composable
internal expect fun rememberLandscapist(httpClient: HttpClient): Landscapist
