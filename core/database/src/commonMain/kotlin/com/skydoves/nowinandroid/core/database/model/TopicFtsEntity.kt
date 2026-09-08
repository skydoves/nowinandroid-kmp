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

package com.skydoves.nowinandroid.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Fts4

/**
 * Fts entity for the topic.
 */
@Entity(tableName = "topicsFts")
// `contentEntity` is spelled out even though `Any::class` is its default: on Kotlin/Native the
// KSP processor cannot resolve the annotation's default value and fails with "Cannot find external
// content entity class".
@Fts4(contentEntity = Any::class)
data class TopicFtsEntity(

    @ColumnInfo(name = "topicId")
    val topicId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "shortDescription")
    val shortDescription: String,

    @ColumnInfo(name = "longDescription")
    val longDescription: String,
)

fun TopicEntity.asFtsEntity() = TopicFtsEntity(
    topicId = id,
    name = name,
    shortDescription = shortDescription,
    longDescription = longDescription,
)
