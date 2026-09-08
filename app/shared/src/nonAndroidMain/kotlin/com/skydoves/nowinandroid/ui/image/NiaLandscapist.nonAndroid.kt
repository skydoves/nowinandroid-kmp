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
import androidx.compose.runtime.remember
import com.skydoves.landscapist.core.Landscapist
import io.ktor.client.HttpClient

@Composable
internal actual fun rememberLandscapist(httpClient: HttpClient): Landscapist =
    // iOS resolves NSCachesDirectory and desktop the user cache directory on their own, so passing
    // no disk cache here lets Landscapist install its platform default rather than override it.
    remember(httpClient) {
        buildNiaLandscapist(httpClient = imageHttpClient(httpClient), diskCache = null)
    }

/**
 * The client image requests go out on.
 *
 * Only the browser needs this to differ: it is the one target with a same-origin policy, so it is
 * the one target that cannot read an image host which sends no `Access-Control-Allow-Origin`.
 */
internal expect fun imageHttpClient(httpClient: HttpClient): HttpClient
