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

package com.skydoves.nowinandroid.core.notifications

import com.skydoves.nowinandroid.core.common.log.NiaLogger
import com.skydoves.nowinandroid.core.model.data.NewsResource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

private const val TAG = "SystemTrayNotifier"

/**
 * Desktop [Notifier] backed by the AWT system tray. Headless environments and desktops without a
 * tray simply get a log line instead of a crash.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SystemTrayNotifier : Notifier {

    private val trayIcon: TrayIcon? by lazy {
        if (!SystemTray.isSupported()) return@lazy null
        runCatching {
            val image = Toolkit.getDefaultToolkit()
                .createImage(ByteArray(0))
            TrayIcon(image, "Now in Android").apply {
                isImageAutoSize = true
                SystemTray.getSystemTray().add(this)
            }
        }.getOrNull()
    }

    override fun postNewsNotifications(newsResources: List<NewsResource>) {
        val truncated = newsResources.take(MAX_NUM_NOTIFICATIONS)
        if (truncated.isEmpty()) return

        val icon = trayIcon
        if (icon == null) {
            truncated.forEach { NiaLogger.info(TAG, "New: ${it.title}") }
            return
        }
        icon.displayMessage(
            newsNotificationGroupSummary(truncated.size),
            truncated.joinToString(separator = "\n") { it.title },
            TrayIcon.MessageType.INFO,
        )
    }
}
