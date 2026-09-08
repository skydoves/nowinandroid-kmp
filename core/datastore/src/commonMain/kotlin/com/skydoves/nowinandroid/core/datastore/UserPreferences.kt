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

import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import kotlinx.serialization.Serializable

/**
 * The persisted shape of the user's preferences.
 *
 * The Android original stored this as a protobuf message, which is javalite-only and therefore
 * JVM-bound. Serialising the same field set with kotlinx.serialization keeps the storage format
 * readable and works on every target. The `map<string, bool>` pseudo-sets from the proto become
 * real `Set<String>`s, and the two legacy "deprecated_*" migration fields are gone: they only
 * existed to upgrade installs of the Android app, which by definition cannot exist here.
 */
@Serializable
data class UserPreferences(
    val followedTopicIds: Set<String> = emptySet(),
    val bookmarkedNewsResourceIds: Set<String> = emptySet(),
    val viewedNewsResourceIds: Set<String> = emptySet(),
    val themeBrand: ThemeBrand = ThemeBrand.DEFAULT,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = false,
    val shouldHideOnboarding: Boolean = false,
    val topicChangeListVersion: Int = 0,
    val newsResourceChangeListVersion: Int = 0,
)
