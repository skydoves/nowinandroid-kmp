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

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.takeFrom
import kotlinx.browser.window

/** Where the topic icons are served from inside this app's own bundle. */
private const val LOCAL_TOPIC_ICONS = "topics"

private const val FIREBASE_STORAGE_HOST = "firebasestorage.googleapis.com"

/**
 * Serves the topic icons from this app's own origin.
 *
 * The demo data points every topic icon at a Firebase Storage bucket that answers browsers without
 * an `Access-Control-Allow-Origin` header, so the fetch is blocked before any decoder sees it. The
 * other three platforms have no same-origin policy and are unaffected.
 *
 * The nineteen icons are therefore copied into `resources/topics/` and requests for them rewritten
 * to point there. Nothing else is touched: hosts that do send the header, such as `i.ytimg.com` and
 * `miro.medium.com`, still load straight from source.
 */
internal actual fun imageHttpClient(httpClient: HttpClient): HttpClient = httpClient.config {
    install(
        createClientPlugin("SameOriginTopicIcons") {
            onRequest { request, _ ->
                val url = request.url
                if (url.host == FIREBASE_STORAGE_HOST) {
                    localIconNameOf(url.encodedPathSegments)?.let { name ->
                        request.url.takeFrom("${window.location.origin}/$LOCAL_TOPIC_ICONS/$name")
                    }
                }
            }
        },
    )
}

/**
 * The object name out of a Firebase Storage path.
 *
 * The path is `/v0/b/<bucket>/o/<object>`, and the object stays percent-encoded, so
 * `img%2Fic_topic_UI.svg` arrives as the single segment after `o`.
 */
private fun localIconNameOf(encodedPathSegments: List<String>): String? {
    val objectIndex = encodedPathSegments.indexOf("o") + 1
    val encodedObject = encodedPathSegments.getOrNull(objectIndex) ?: return null
    val name = encodedObject.replace("%2F", "/").substringAfterLast('/')
    return name.takeIf { it.endsWith(".svg") }
}
