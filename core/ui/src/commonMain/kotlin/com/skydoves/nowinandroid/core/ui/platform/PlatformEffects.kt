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

package com.skydoves.nowinandroid.core.ui.platform

import androidx.compose.runtime.Composable

/**
 * Signals that the UI is ready for use, for Time To Full Display metrics. Android reports this to
 * the framework; elsewhere there is nothing listening.
 */
@Composable
expect fun ReportFullyDrawnWhen(predicate: () -> Boolean)

/**
 * Asks for the notification permission the first time the feed is shown. Only Android has a
 * runtime notification permission to ask for.
 */
@Composable
expect fun NotificationPermissionEffect()
