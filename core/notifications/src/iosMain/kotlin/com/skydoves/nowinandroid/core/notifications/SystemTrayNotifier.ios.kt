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

import com.skydoves.nowinandroid.core.model.data.NewsResource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS [Notifier] backed by `UNUserNotificationCenter`. One local notification per news item, which
 * iOS itself groups by thread identifier the way Android groups by notification group.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SystemTrayNotifier : Notifier {

    override fun postNewsNotifications(newsResources: List<NewsResource>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or
                UNAuthorizationOptionSound or
                UNAuthorizationOptionBadge,
        ) { granted, _ ->
            if (!granted) return@requestAuthorizationWithOptions
            newsResources.take(MAX_NUM_NOTIFICATIONS).forEach { newsResource ->
                val content = UNMutableNotificationContent().apply {
                    setTitle(newsResource.title)
                    setBody(newsResource.content)
                    setThreadIdentifier(NEWS_THREAD_IDENTIFIER)
                    setUserInfo(mapOf(DEEP_LINK_NEWS_RESOURCE_ID_KEY to newsResource.id))
                }
                center.addNotificationRequest(
                    request = UNNotificationRequest.requestWithIdentifier(
                        identifier = newsResource.id,
                        content = content,
                        trigger = UNTimeIntervalNotificationTrigger
                            .triggerWithTimeInterval(timeInterval = 1.0, repeats = false),
                    ),
                    withCompletionHandler = null,
                )
            }
        }
    }

    private companion object {
        const val NEWS_THREAD_IDENTIFIER = "NEWS_NOTIFICATIONS"
    }
}
