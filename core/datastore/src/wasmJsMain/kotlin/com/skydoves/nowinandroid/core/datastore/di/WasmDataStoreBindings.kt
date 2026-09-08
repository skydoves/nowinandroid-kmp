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
import androidx.datastore.core.okio.OkioSerializer
import com.skydoves.nowinandroid.core.datastore.UserPreferences
import com.skydoves.nowinandroid.core.datastore.UserPreferencesSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Buffer

@BindingContainer
@ContributesTo(AppScope::class)
object WasmDataStoreBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun providesUserPreferencesDataStore(): DataStore<UserPreferences> =
        LocalStorageDataStore(USER_PREFERENCES_FILE_NAME, UserPreferencesSerializer)
}

/**
 * Preferences in `localStorage`.
 *
 * This does not build on `DataStoreFactory`, whose wasm actual is `TODO("Not yet implemented")` in
 * DataStore 1.2.1, and it does not need to: `DataStore` is an interface of two members, and a page
 * is single threaded, so a mutex and a cached value cover what the factory's file machinery exists
 * to solve.
 *
 * The bytes are still produced by [UserPreferencesSerializer], the same serializer the other three
 * platforms use. It writes to an okio `BufferedSink`, and an in-memory [Buffer] turns that into the
 * string `localStorage` holds, so the stored format is identical everywhere.
 */
private class LocalStorageDataStore(private val key: String, private val serializer: OkioSerializer<UserPreferences>) :
    DataStore<UserPreferences> {

    private val mutex = Mutex()
    private val cache = MutableStateFlow<UserPreferences?>(null)

    override val data: Flow<UserPreferences> = cache
        .onStart { mutex.withLock { if (cache.value == null) cache.value = load() } }
        .filterNotNull()

    override suspend fun updateData(transform: suspend (UserPreferences) -> UserPreferences): UserPreferences =
        mutex.withLock {
            val updated = transform(cache.value ?: load())
            val buffer = Buffer()
            serializer.writeTo(updated, buffer)
            localStorage.setItem(key, buffer.readUtf8())
            cache.value = updated
            updated
        }

    /** Unreadable stored preferences fall back to defaults, rather than leaving the app unusable. */
    private suspend fun load(): UserPreferences {
        val stored = localStorage.getItem(key) ?: return serializer.defaultValue
        return runCatching { serializer.readFrom(Buffer().apply { writeUtf8(stored) }) }
            .getOrElse { serializer.defaultValue }
    }
}
