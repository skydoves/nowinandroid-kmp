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

package com.skydoves.nowinandroid

import android.app.Application
import com.skydoves.nowinandroid.core.data.sync.initializeSync
import com.skydoves.nowinandroid.di.AndroidAppGraph
import dev.zacsweers.metro.createGraphFactory

class NiaApplication : Application() {

    lateinit var appGraph: AndroidAppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AndroidAppGraph.Factory>().create(this)
        // The Android original enqueued a one-off `SyncWorker` from `Sync.initialize`.
        appGraph.syncManager.initializeSync()
    }
}
