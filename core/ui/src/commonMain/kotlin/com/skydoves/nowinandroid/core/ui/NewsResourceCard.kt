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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import com.skydoves.nowinandroid.core.designsystem.component.NiaIconToggleButton
import com.skydoves.nowinandroid.core.designsystem.component.NiaTopicTag
import com.skydoves.nowinandroid.core.designsystem.component.cursorHoverIcon
import com.skydoves.nowinandroid.core.designsystem.component.rememberNiaShimmer
import com.skydoves.nowinandroid.core.designsystem.generated.resources.core_designsystem_ic_placeholder_default
import com.skydoves.nowinandroid.core.designsystem.icon.NiaIcons
import com.skydoves.nowinandroid.core.designsystem.theme.NiaTheme
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.core.model.data.NewsResource
import com.skydoves.nowinandroid.core.model.data.UserNewsResource
import com.skydoves.nowinandroid.core.ui.generated.resources.Res
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_bookmark
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_card_meta_data_text
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_card_tap_action
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_feed_sharing
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_feed_sharing_data
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_topic_chip_content_description_when_followed
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_topic_chip_content_description_when_not_followed
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_unbookmark
import com.skydoves.nowinandroid.core.ui.generated.resources.core_ui_unread_resource_dot_content_description
import com.skydoves.nowinandroid.core.ui.platform.newsResourceDragAndDropSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import com.skydoves.nowinandroid.core.designsystem.generated.resources.Res as DesignSystemRes

/**
 * [NewsResource] card used on the following screens: For You, Saved
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsResourceCardExpanded(
    userNewsResource: UserNewsResource,
    isBookmarked: Boolean,
    hasBeenViewed: Boolean,
    onToggleBookmark: () -> Unit,
    onClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickActionLabel = stringResource(Res.string.core_ui_card_tap_action)
    val sharingLabel = stringResource(Res.string.core_ui_feed_sharing)
    val sharingContent = stringResource(
        Res.string.core_ui_feed_sharing_data,
        userNewsResource.title,
        userNewsResource.url,
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // Use custom label for accessibility services to communicate button's action to user.
        // Pass null for action to only override the label and not the actual action.
        modifier = modifier
            .cursorHoverIcon()
            .semantics {
                onClick(label = clickActionLabel, action = null)
            }
            .testTag("newsResourceCard:${userNewsResource.id}"),
    ) {
        Column {
            if (!userNewsResource.headerImageUrl.isNullOrEmpty()) {
                Row {
                    NewsResourceHeaderImage(userNewsResource.headerImageUrl)
                }
            }
            Box(
                modifier = Modifier.padding(16.dp),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        NewsResourceTitle(
                            userNewsResource.title,
                            modifier = Modifier
                                .fillMaxWidth((.8f))
                                .newsResourceDragAndDropSource(sharingLabel, sharingContent),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        BookmarkButton(isBookmarked, onToggleBookmark)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!hasBeenViewed) {
                            NotificationDot(
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(8.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                        NewsResourceMetaData(userNewsResource.publishDate, userNewsResource.type)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    NewsResourceShortDescription(userNewsResource.content)
                    Spacer(modifier = Modifier.height(12.dp))
                    NewsResourceTopics(
                        topics = userNewsResource.followableTopics,
                        onTopicClick = onTopicClick,
                    )
                }
            }
        }
    }
}

@Composable
fun NewsResourceHeaderImage(headerImageUrl: String?) {
    val placeholder = painterResource(
        DesignSystemRes.drawable.core_designsystem_ic_placeholder_default,
    )
    val isLocalInspection = LocalInspectionMode.current
    val shimmer = rememberNiaShimmer()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLocalInspection || headerImageUrl.isNullOrEmpty()) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                painter = placeholder,
                // TODO b/226661685: Investigate using alt text of image to populate content
                // description; decorative image for now.
                contentDescription = null,
            )
            return@Box
        }

        LandscapistImage(
            imageModel = { headerImageUrl },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            component = rememberImageComponent {
                +ShimmerPlugin(shimmer = shimmer)
            },
            failure = {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                    painter = placeholder,
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
fun NewsResourceTitle(newsResourceTitle: String, modifier: Modifier = Modifier) {
    Text(newsResourceTitle, style = MaterialTheme.typography.headlineSmall, modifier = modifier)
}

@Composable
fun BookmarkButton(isBookmarked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    NiaIconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { onClick() },
        modifier = modifier,
        icon = {
            Icon(
                imageVector = NiaIcons.BookmarkBorder,
                contentDescription = stringResource(Res.string.core_ui_bookmark),
            )
        },
        checkedIcon = {
            Icon(
                imageVector = NiaIcons.Bookmark,
                contentDescription = stringResource(Res.string.core_ui_unbookmark),
            )
        },
    )
}

@Composable
fun NotificationDot(color: Color, modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.core_ui_unread_resource_dot_content_description)
    Canvas(
        modifier = modifier
            .semantics { contentDescription = description },
        onDraw = {
            drawCircle(
                color,
                radius = size.minDimension / 2,
            )
        },
    )
}

/**
 * `java.time`'s localized MEDIUM date style has no multiplatform equivalent, so the same shape
 * ("Oct 5, 2022") is spelled out with kotlinx-datetime's format builder.
 */
internal val MediumDateFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    // `java.time`'s MEDIUM style does not pad the day, so neither does this.
    day(Padding.NONE)
    chars(", ")
    year()
}

@Composable
fun dateFormatted(publishDate: Instant): String =
    publishDate.toLocalDateTime(LocalTimeZone.current).date.format(MediumDateFormat)

@Composable
fun NewsResourceMetaData(publishDate: Instant, resourceType: String) {
    val formattedDate = dateFormatted(publishDate)
    Text(
        if (resourceType.isNotBlank()) {
            stringResource(Res.string.core_ui_card_meta_data_text, formattedDate, resourceType)
        } else {
            formattedDate
        },
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
fun NewsResourceShortDescription(newsResourceShortDescription: String) {
    Text(newsResourceShortDescription, style = MaterialTheme.typography.bodyLarge)
}

@Composable
fun NewsResourceTopics(topics: List<FollowableTopic>, onTopicClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        // causes narrow chips
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (followableTopic in topics) {
            NiaTopicTag(
                followed = followableTopic.isFollowed,
                onClick = { onTopicClick(followableTopic.topic.id) },
                text = {
                    val contentDescription = if (followableTopic.isFollowed) {
                        stringResource(
                            Res.string.core_ui_topic_chip_content_description_when_followed,
                            followableTopic.topic.name,
                        )
                    } else {
                        stringResource(
                            Res.string.core_ui_topic_chip_content_description_when_not_followed,
                            followableTopic.topic.name,
                        )
                    }
                    Text(
                        text = followableTopic.topic.name.uppercase(),
                        modifier = Modifier
                            .semantics {
                                this.contentDescription = contentDescription
                            }
                            .testTag("topicTag:${followableTopic.topic.id}"),
                    )
                },
            )
        }
    }
}

@Preview("Bookmark Button")
@Composable
private fun BookmarkButtonPreview() {
    NiaTheme {
        Surface {
            BookmarkButton(isBookmarked = false, onClick = { })
        }
    }
}

@Preview("Bookmark Button Bookmarked")
@Composable
private fun BookmarkButtonBookmarkedPreview() {
    NiaTheme {
        Surface {
            BookmarkButton(isBookmarked = true, onClick = { })
        }
    }
}

@Preview("NewsResourceCardExpanded")
@Composable
private fun ExpandedNewsResourcePreview(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    CompositionLocalProvider(
        LocalInspectionMode provides true,
    ) {
        NiaTheme {
            Surface {
                NewsResourceCardExpanded(
                    userNewsResource = userNewsResources[0],
                    isBookmarked = true,
                    hasBeenViewed = false,
                    onToggleBookmark = {},
                    onClick = {},
                    onTopicClick = {},
                )
            }
        }
    }
}
