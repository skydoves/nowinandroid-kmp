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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.skydoves.nowinandroid.core.data.deeplink.DeepLinkStore
import com.skydoves.nowinandroid.core.notifications.DEEP_LINK_BASE_PATH
import com.skydoves.nowinandroid.ui.NiaAppRoot

class MainActivity : ComponentActivity() {

    private lateinit var deepLinkStore: DeepLinkStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appGraph = (application as NiaApplication).appGraph
        deepLinkStore = appGraph.deepLinkStore
        handleDeepLink(intent)

        setContent {
            NiaAppRoot(appGraph)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * A notification tap arrives as `.../foryou/{newsResourceId}`; the shared
     * [DeepLinkStore] is what the For You screen observes.
     */
    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data?.toString() ?: return
        if (!data.startsWith(DEEP_LINK_BASE_PATH)) return
        val newsResourceId = data.removePrefix("$DEEP_LINK_BASE_PATH/").takeIf { it.isNotEmpty() }
        deepLinkStore.submit(newsResourceId)
    }
}
