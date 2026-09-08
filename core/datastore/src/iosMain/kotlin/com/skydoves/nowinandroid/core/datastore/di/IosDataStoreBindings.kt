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

package com.skydoves.nowinandroid.core.datastore.di

import androidx.datastore.core.DataStore
import com.skydoves.nowinandroid.core.common.network.ApplicationScope
import com.skydoves.nowinandroid.core.common.network.IoDispatcher
import com.skydoves.nowinandroid.core.datastore.UserPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import okio.FileSystem
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@BindingContainer
@ContributesTo(AppScope::class)
object IosDataStoreBindings {

    @Provides
    fun providesDataStorePathProducer(): DataStorePathProducer = DataStorePathProducer { fileName ->
        "${documentDirectory()}/$fileName"
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(url?.path) { "Unable to resolve the iOS documents directory" }
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesUserPreferencesDataStore(
        pathProducer: DataStorePathProducer,
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DataStore<UserPreferences> = okioUserPreferencesDataStore(
        fileSystem = FileSystem.SYSTEM,
        pathProducer = pathProducer,
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
    )
}
