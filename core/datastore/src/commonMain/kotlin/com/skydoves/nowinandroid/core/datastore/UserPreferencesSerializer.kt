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

package com.skydoves.nowinandroid.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

/**
 * An [OkioSerializer] for [UserPreferences], backed by kotlinx.serialization rather than protobuf
 * so the same code runs on Android, iOS and the desktop JVM.
 */
internal object UserPreferencesSerializer : OkioSerializer<UserPreferences> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: UserPreferences = UserPreferences()

    override suspend fun readFrom(source: BufferedSource): UserPreferences = try {
        json.decodeFromString(UserPreferences.serializer(), source.readUtf8())
    } catch (exception: SerializationException) {
        throw CorruptionException("Cannot read user preferences.", exception)
    }

    override suspend fun writeTo(t: UserPreferences, sink: BufferedSink) {
        sink.writeUtf8(json.encodeToString(UserPreferences.serializer(), t))
    }
}
