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
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import com.skydoves.nowinandroid.core.datastore.UserPreferences
import com.skydoves.nowinandroid.core.datastore.UserPreferencesSerializer
import kotlinx.coroutines.CoroutineScope
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Resolves the absolute path of a file in the app's private storage. Only the platform knows where
 * that is, so each one contributes its own implementation.
 */
fun interface DataStorePathProducer {
    fun producePath(fileName: String): String
}

internal const val USER_PREFERENCES_FILE_NAME = "user_preferences.json"

/**
 * The preferences store for a platform that has a real filesystem.
 *
 * `DataStoreFactory` is deliberately not called from common code. Its wasm actual is
 * `TODO("Not yet implemented")` in DataStore 1.2.1, so a browser build reaching this would compile
 * and then throw on first use. Each platform names its own store instead, and the browser supplies
 * one that does not go through the factory at all.
 *
 * The [FileSystem] is a parameter rather than `FileSystem.SYSTEM` because okio declares no `SYSTEM`
 * on a platform without files.
 */
fun okioUserPreferencesDataStore(
    fileSystem: FileSystem,
    pathProducer: DataStorePathProducer,
    scope: CoroutineScope,
): DataStore<UserPreferences> = DataStoreFactory.create(
    storage = OkioStorage(
        fileSystem = fileSystem,
        serializer = UserPreferencesSerializer,
        producePath = { pathProducer.producePath(USER_PREFERENCES_FILE_NAME).toPath() },
    ),
    scope = scope,
)
