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

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import com.skydoves.nowinandroid.core.common.network.IoDispatcher
import com.skydoves.nowinandroid.core.database.NiaDatabase
import com.skydoves.nowinandroid.core.database.dao.NewsResourceDao
import com.skydoves.nowinandroid.core.database.dao.NewsResourceFtsDao
import com.skydoves.nowinandroid.core.database.dao.RecentSearchQueryDao
import com.skydoves.nowinandroid.core.database.dao.TopicDao
import com.skydoves.nowinandroid.core.database.dao.TopicFtsDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher

@BindingContainer
@ContributesTo(AppScope::class)
object DatabaseBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun providesNiaDatabase(
        builder: RoomDatabase.Builder<NiaDatabase>,
        // The driver is a binding rather than a constant: the bundled SQLite is a native library,
        // so a target without one supplies its own.
        driver: SQLiteDriver,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): NiaDatabase = builder
        .setDriver(driver)
        .setQueryCoroutineContext(ioDispatcher)
        .build()

    @Provides
    fun providesTopicsDao(database: NiaDatabase): TopicDao = database.topicDao()

    @Provides
    fun providesNewsResourceDao(database: NiaDatabase): NewsResourceDao = database.newsResourceDao()

    @Provides
    fun providesTopicFtsDao(database: NiaDatabase): TopicFtsDao = database.topicFtsDao()

    @Provides
    fun providesNewsResourceFtsDao(database: NiaDatabase): NewsResourceFtsDao = database.newsResourceFtsDao()

    @Provides
    fun providesRecentSearchQueryDao(database: NiaDatabase): RecentSearchQueryDao = database.recentSearchQueryDao()
}
