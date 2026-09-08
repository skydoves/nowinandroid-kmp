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

package com.skydoves.nowinandroid.core.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.skydoves.nowinandroid.core.database.dao.NewsResourceDao
import com.skydoves.nowinandroid.core.database.dao.NewsResourceFtsDao
import com.skydoves.nowinandroid.core.database.dao.RecentSearchQueryDao
import com.skydoves.nowinandroid.core.database.dao.TopicDao
import com.skydoves.nowinandroid.core.database.dao.TopicFtsDao
import com.skydoves.nowinandroid.core.database.model.NewsResourceEntity
import com.skydoves.nowinandroid.core.database.model.NewsResourceFtsEntity
import com.skydoves.nowinandroid.core.database.model.NewsResourceTopicCrossRef
import com.skydoves.nowinandroid.core.database.model.RecentSearchQueryEntity
import com.skydoves.nowinandroid.core.database.model.TopicEntity
import com.skydoves.nowinandroid.core.database.model.TopicFtsEntity
import com.skydoves.nowinandroid.core.database.util.InstantConverter

/**
 * The schema starts again at version 1: the Android original's thirteen auto-migrations exist to
 * upgrade installs of the Android app, and no such install can be upgraded into this one.
 */
@Database(
    entities = [
        NewsResourceEntity::class,
        NewsResourceTopicCrossRef::class,
        NewsResourceFtsEntity::class,
        TopicEntity::class,
        TopicFtsEntity::class,
        RecentSearchQueryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ColumnTypeConverters(value = [InstantConverter::class])
@ConstructedBy(NiaDatabaseConstructor::class)
abstract class NiaDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun newsResourceDao(): NewsResourceDao
    abstract fun topicFtsDao(): TopicFtsDao
    abstract fun newsResourceFtsDao(): NewsResourceFtsDao
    abstract fun recentSearchQueryDao(): RecentSearchQueryDao
}

/**
 * Room generates the `actual` for every target it processes, so the expect declaration only has to
 * name the contract.
 */
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_IR_INCOMPATIBILITY")
expect object NiaDatabaseConstructor : RoomDatabaseConstructor<NiaDatabase> {
    override fun initialize(): NiaDatabase
}

const val NIA_DATABASE_NAME: String = "nia-database.db"
