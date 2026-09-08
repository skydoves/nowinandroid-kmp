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

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope

/**
 * The slice of `androidx.metrics.performance.PerformanceMetricsState` this app actually uses.
 * Android reports real jank state; every other target gets a no-op.
 */
interface JankMetricsState {
    fun putState(key: String, value: String)
    fun removeState(key: String)
}

@Composable
expect fun rememberMetricsStateHolder(): JankMetricsState

/**
 * Convenience function to work with [JankMetricsState]. The side effect is re-launched if any of
 * the [keys] value is not equal to the previous composition.
 * @see TrackDisposableJank if you need to work with DisposableEffect to clean up added state.
 */
@Composable
fun TrackJank(vararg keys: Any, reportMetric: suspend CoroutineScope.(state: JankMetricsState) -> Unit) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(metrics, *keys) {
        reportMetric(metrics)
    }
}

/**
 * Convenience function to work with [JankMetricsState] that needs to be cleaned up. The side
 * effect is re-launched if any of the [keys] value is not equal to the previous composition.
 */
@Composable
fun TrackDisposableJank(
    vararg keys: Any,
    reportMetric: DisposableEffectScope.(state: JankMetricsState) -> DisposableEffectResult,
) {
    val metrics = rememberMetricsStateHolder()
    DisposableEffect(metrics, *keys) {
        reportMetric(this, metrics)
    }
}

/**
 * Track jank while scrolling anything that's scrollable.
 */
@Composable
fun TrackScrollJank(scrollableState: ScrollableState, stateName: String) {
    TrackJank(scrollableState) { metricsHolder ->
        snapshotFlow { scrollableState.isScrollInProgress }.collect { isScrollInProgress ->
            if (isScrollInProgress) {
                metricsHolder.putState(stateName, "Scrolling=true")
            } else {
                metricsHolder.removeState(stateName)
            }
        }
    }
}
