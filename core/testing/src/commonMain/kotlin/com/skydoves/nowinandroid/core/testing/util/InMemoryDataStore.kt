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

package com.skydoves.nowinandroid.core.testing.util

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A [DataStore] that keeps its value in memory, so preference tests do not need a file system.
 */
class InMemoryDataStore<T>(initialValue: T) : DataStore<T> {

    private val state = MutableStateFlow(initialValue)
    private val mutex = Mutex()

    override val data: Flow<T> = state.asStateFlow()

    override suspend fun updateData(transform: suspend (t: T) -> T): T = mutex.withLock {
        state.value = transform(state.value)
        state.value
    }
}
