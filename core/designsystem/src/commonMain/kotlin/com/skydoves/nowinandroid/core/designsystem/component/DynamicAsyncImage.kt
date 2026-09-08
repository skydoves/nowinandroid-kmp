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

package com.skydoves.nowinandroid.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import com.skydoves.nowinandroid.core.designsystem.generated.resources.Res
import com.skydoves.nowinandroid.core.designsystem.generated.resources.core_designsystem_ic_placeholder_default
import com.skydoves.nowinandroid.core.designsystem.theme.LocalTintTheme
import org.jetbrains.compose.resources.painterResource

/**
 * The shimmer every remote image shows while it loads.
 *
 * Upstream filled the loading slot with an 80.dp [androidx.compose.material3.CircularProgressIndicator].
 * That size was chosen for the 180.dp news header, where it still covers most of the card and blinks
 * on every scroll; over a 32.dp topic icon the same spinner is clamped to the icon and reads as a
 * stray ring. A shimmer takes the shape of whatever box it is given, so one definition is right at
 * both sizes and the placeholder reads as "a picture is arriving here".
 */
@Composable
fun rememberNiaShimmer(): Shimmer {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface
    return remember(baseColor, highlightColor) {
        Shimmer.Resonate(baseColor = baseColor, highlightColor = highlightColor)
    }
}

/**
 * A wrapper around Landscapist's [LandscapistImage] which determines the colorFilter based on the
 * theme.
 *
 * The Android original used an image loader that only runs on Android. Landscapist's own loading
 * engine gives the same loading and error slots on every target.
 */
@Composable
fun DynamicAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter = painterResource(
        Res.drawable.core_designsystem_ic_placeholder_default,
    ),
) {
    val iconTint = LocalTintTheme.current.iconTint
    val colorFilter = if (iconTint != Unspecified) ColorFilter.tint(iconTint) else null
    val isLocalInspection = LocalInspectionMode.current
    val shimmer = rememberNiaShimmer()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (isLocalInspection) {
            // Previews and screenshot tests have no network, so they render the placeholder.
            Image(
                painter = placeholder,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter,
            )
            return@Box
        }

        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier.fillMaxSize(),
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                contentDescription = contentDescription,
                colorFilter = colorFilter,
            ),
            component = rememberImageComponent {
                +ShimmerPlugin(shimmer = shimmer)
            },
            failure = {
                Image(
                    painter = placeholder,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                )
            },
        )
    }
}
