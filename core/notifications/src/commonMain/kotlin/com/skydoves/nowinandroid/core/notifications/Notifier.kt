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

/**
 * Interface for creating notifications in the app
 */
interface Notifier {
    fun postNewsNotifications(newsResources: List<NewsResource>)
}

/** How many news items a single sync is allowed to notify about. */
internal const val MAX_NUM_NOTIFICATIONS = 5

internal const val NEWS_NOTIFICATION_CHANNEL_NAME = "News updates"
internal const val NEWS_NOTIFICATION_CHANNEL_DESCRIPTION =
    "The latest updates on what's new in Android"

internal fun newsNotificationGroupSummary(count: Int) = "$count news updates"

private const val DEEP_LINK_SCHEME_AND_HOST = "https://www.nowinandroid.apps.samples.google.com"
private const val DEEP_LINK_FOR_YOU_PATH = "foryou"

const val DEEP_LINK_BASE_PATH: String = "$DEEP_LINK_SCHEME_AND_HOST/$DEEP_LINK_FOR_YOU_PATH"
const val DEEP_LINK_NEWS_RESOURCE_ID_KEY: String = "linkedNewsResourceId"
const val DEEP_LINK_URI_PATTERN: String = "$DEEP_LINK_BASE_PATH/{$DEEP_LINK_NEWS_RESOURCE_ID_KEY}"

fun NewsResource.newsDeepLink(): String = "$DEEP_LINK_BASE_PATH/$id"
