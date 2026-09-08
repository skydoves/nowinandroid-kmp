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

package com.skydoves.nowinandroid.ui.image

import com.skydoves.landscapist.core.LandscapistConfig
import com.skydoves.landscapist.core.decoder.DecodeResult
import com.skydoves.landscapist.svg.SvgImageDecoder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The app's decoder chain, exercised end to end.
 *
 * These run on the desktop JVM and the iOS simulator rather than in `commonTest`: rasterising needs
 * real graphics, and an Android *host* test has only stubbed `android.graphics` classes.
 */
class NiaImagePipelineTest {

    private val decoder = SvgImageDecoder()

    private val topicIcon = """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 24">
          <rect width="48" height="24" fill="#3DDC84"/>
        </svg>
    """.trimIndent().encodeToByteArray()

    @Test
    fun `an svg served without a content type still decodes`() = runTest {
        // The topic icons come back from Firebase without a usable content type, so the decoder has
        // to recognise the markup from the bytes alone.
        val result = decoder.decode(topicIcon, null, 96, 48, LandscapistConfig())

        assertTrue(result is DecodeResult.Success, "expected a bitmap, got $result")
        assertEquals(96, result.width)
        assertEquals(48, result.height)
    }

    @Test
    fun `an svg renders at the size the layout asked for rather than its own`() = runTest {
        // A vector has no natural resolution: a 48x24 document placed in a 192px-wide slot must
        // rasterise at 192, otherwise the icons are blurry on the topic header.
        val result = decoder.decode(topicIcon, "image/svg+xml", 192, null, LandscapistConfig())

        assertTrue(result is DecodeResult.Success)
        assertEquals(192, result.width)
        // The unbounded axis follows the document's own 2:1 ratio rather than stretching.
        assertEquals(96, result.height)
    }

    @Test
    fun `a raster image goes to the platform decoder and comes back drawable`() = runTest {
        val result = decoder.decode(ONE_PIXEL_PNG, "image/png", null, null, LandscapistConfig())

        // Desktop decodes to a BufferedImage while iOS returns encoded bytes, so this pins that
        // both come back as something the painter can actually draw.
        assertTrue(result is DecodeResult.Success, "expected a bitmap, got $result")
        assertEquals(1, result.width)
        assertEquals(1, result.height)
    }

    @Test
    fun `markup that is not an svg at all fails rather than rendering blank`() = runTest {
        val result = decoder.decode(
            "<svg>this is not closed and not valid".encodeToByteArray(),
            "image/svg+xml",
            32,
            32,
            LandscapistConfig(),
        )

        assertTrue(result is DecodeResult.Error, "expected an error, got $result")
    }
}

/** A 1x1 opaque PNG, small enough to inline and enough to prove the raster path end to end. */
private val ONE_PIXEL_PNG: ByteArray = byteArrayOf(
    0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
    0x08, 0x02, 0x00, 0x00, 0x00, 0x90.toByte(), 0x77, 0x53,
    0xDE.toByte(), 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
    0x54, 0x08, 0xD7.toByte(), 0x63, 0xF8.toByte(), 0xCF.toByte(), 0xC0.toByte(), 0x00,
    0x00, 0x03, 0x01, 0x01, 0x00, 0x18.toByte(), 0xDD.toByte(), 0x8D.toByte(),
    0xB0.toByte(), 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
    0x44, 0xAE.toByte(), 0x42, 0x60, 0x82.toByte(),
)
