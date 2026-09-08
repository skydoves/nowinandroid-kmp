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

package com.skydoves.nowinandroid.feature.topic.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

/**
 * The topic screen used to answer [TopicUiState.Error] with `TODO()`, so a topic that failed to load
 * took the app down with it. This pins that it renders instead.
 *
 * It runs under a real composition, which needs a window, so it lives off Android alongside the
 * other `runComposeUiTest` cases.
 */
class TopicScreenErrorTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun topicErrorStateRendersAMessageRatherThanCrashing() = runComposeUiTest {
        setContent {
            TopicScreen(
                topicUiState = TopicUiState.Error,
                newsUiState = NewsUiState.Error,
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
            )
        }

        onNodeWithText("Could not load this topic. Check your connection and try again.")
            .assertExists()
    }
}
