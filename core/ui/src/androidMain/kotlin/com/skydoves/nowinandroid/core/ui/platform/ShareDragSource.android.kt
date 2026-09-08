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

import android.content.ClipData
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData

@Composable
actual fun Modifier.newsResourceDragAndDropSource(label: String, content: String): Modifier {
    val dragAndDropFlags = if (VERSION.SDK_INT >= VERSION_CODES.N) View.DRAG_FLAG_GLOBAL else 0
    return dragAndDropSource { _ ->
        DragAndDropTransferData(
            ClipData.newPlainText(label, content),
            flags = dragAndDropFlags,
        )
    }
}
