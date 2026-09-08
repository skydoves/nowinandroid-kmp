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
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.skydoves.nowinandroid.core.database.NIA_DATABASE_NAME
import com.skydoves.nowinandroid.core.database.NiaDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@BindingContainer
@ContributesTo(AppScope::class)
object IosDatabaseBindings {

    @Provides
    fun providesDatabaseBuilder(): RoomDatabase.Builder<NiaDatabase> =
        Room.databaseBuilder(name = "${documentDirectory()}/$NIA_DATABASE_NAME")

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
    fun providesSQLiteDriver(): SQLiteDriver = BundledSQLiteDriver()
}
