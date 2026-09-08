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

package com.skydoves.nowinandroid.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * JankStats is an Android library with no counterpart elsewhere, so the tracking calls compile
 * away to nothing on the desktop and on iOS.
 */
@Composable
actual fun rememberMetricsStateHolder(): JankMetricsState = remember {
    object : JankMetricsState {
        override fun putState(key: String, value: String) = Unit
        override fun removeState(key: String) = Unit
    }
}
