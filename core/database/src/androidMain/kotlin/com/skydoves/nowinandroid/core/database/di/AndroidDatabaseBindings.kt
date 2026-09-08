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

import android.content.Context
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

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidDatabaseBindings {

    @Provides
    fun providesDatabaseBuilder(context: Context): RoomDatabase.Builder<NiaDatabase> = Room.databaseBuilder(
        context = context.applicationContext,
        name = context.getDatabasePath(NIA_DATABASE_NAME).absolutePath,
    )

    @Provides
    fun providesSQLiteDriver(): SQLiteDriver = BundledSQLiteDriver()
}
