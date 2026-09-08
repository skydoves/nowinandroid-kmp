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

package com.skydoves.nowinandroid.core.data.repository

import com.skydoves.nowinandroid.core.data.Synchronizer
import com.skydoves.nowinandroid.core.datastore.ChangeListVersions
import com.skydoves.nowinandroid.core.datastore.NiaPreferencesDataSource

/**
 * Test synchronizer that delegates to [NiaPreferencesDataSource]
 */
class TestSynchronizer(private val niaPreferences: NiaPreferencesDataSource) : Synchronizer {
    override suspend fun getChangeListVersions(): ChangeListVersions = niaPreferences.getChangeListVersions()

    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) =
        niaPreferences.updateChangeListVersion(update)
}
