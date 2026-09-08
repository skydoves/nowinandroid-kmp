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

package com.skydoves.nowinandroid.feature.interests.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.nowinandroid.core.designsystem.theme.NiaTheme
import com.skydoves.nowinandroid.feature.interests.api.generated.resources.feature_interests_api_ic_detail_placeholder
import com.skydoves.nowinandroid.feature.interests.api.generated.resources.feature_interests_api_select_an_interest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.skydoves.nowinandroid.feature.interests.api.generated.resources.Res as InterestsApiRes

@Composable
fun InterestsDetailPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                20.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Icon(
                painter = painterResource(InterestsApiRes.drawable.feature_interests_api_ic_detail_placeholder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(InterestsApiRes.string.feature_interests_api_select_an_interest),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Preview(widthDp = 200, heightDp = 300)
@Composable
fun TopicDetailPlaceholderPreview() {
    NiaTheme {
        InterestsDetailPlaceholder()
    }
}
