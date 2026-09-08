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
import androidx.compose.ui.Modifier

/**
 * Makes a composable the source of a system drag carrying [content], so the user can drag a news
 * item's title into another app. Only Android implements system-wide drag and drop from Compose;
 * elsewhere this is the identity modifier.
 */
@Composable
expect fun Modifier.newsResourceDragAndDropSource(label: String, content: String): Modifier
