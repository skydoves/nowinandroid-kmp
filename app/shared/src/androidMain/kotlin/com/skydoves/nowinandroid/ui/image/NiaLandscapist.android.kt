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
import androidx.compose.ui.platform.LocalContext
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.LandscapistConfig
import com.skydoves.landscapist.core.cache.DiskLruCache
import io.ktor.client.HttpClient
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

@Composable
internal actual fun rememberLandscapist(httpClient: HttpClient): Landscapist {
    // Landscapist's default disk cache is null on Android, because it has no way to reach a Context, so
    // the cache directory is supplied here, the same place the framework would put it.
    val context = LocalContext.current.applicationContext
    return remember(httpClient, context) {
        val cacheDirectory = File(context.cacheDir, "landscapist_cache")
        buildNiaLandscapist(
            httpClient = httpClient,
            diskCache = DiskLruCache.create(
                directory = cacheDirectory.toOkioPath(),
                maxSize = LandscapistConfig().diskCacheSize,
                fileSystem = FileSystem.SYSTEM,
            ),
        )
    }
}
