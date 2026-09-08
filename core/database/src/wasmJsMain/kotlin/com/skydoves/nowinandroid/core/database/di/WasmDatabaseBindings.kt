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

package com.skydoves.nowinandroid.core.database.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.skydoves.nowinandroid.core.database.NIA_DATABASE_NAME
import com.skydoves.nowinandroid.core.database.NiaDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import org.w3c.dom.MODULE
import org.w3c.dom.Worker
import org.w3c.dom.WorkerOptions
import org.w3c.dom.WorkerType

/**
 * The script the SQLite WASM build runs in. It is served from the web app's static resources
 * rather than bundled by Kotlin, because a `Worker` is constructed from a URL the page can fetch.
 */
private const val SQLITE_WORKER_SCRIPT = "sqlite-worker.js"

@BindingContainer
@ContributesTo(AppScope::class)
object WasmDatabaseBindings {

    @Provides
    fun providesDatabaseBuilder(): RoomDatabase.Builder<NiaDatabase> = Room.databaseBuilder(name = NIA_DATABASE_NAME)

    /**
     * Single instance because it owns a `Worker`: every connection is a message round trip to that
     * one thread, and starting a second worker would mean a second, unrelated database.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun providesSQLiteDriver(): SQLiteDriver = WebWorkerSQLiteDriver(
        // A module worker, because the official sqlite-wasm build is ESM only and so the worker
        // has to `import` it rather than use `importScripts`.
        Worker(SQLITE_WORKER_SCRIPT, WorkerOptions(type = WorkerType.MODULE)),
    )
}
