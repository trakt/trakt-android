package tv.trakt.trakt.ui.components.mediacards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Type
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@Composable
internal fun CustomListCard(
    list: CustomList,
    modifier: Modifier = Modifier,
    likesVisible: Boolean = false,
    userVisible: Boolean = true,
    descriptionVisible: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = cardColors(
            containerColor = TraktTheme.colors.customListContainer,
        ),
        content = {
            CustomListContent(
                list = list,
                likesVisible = likesVisible,
                userVisible = userVisible,
                descriptionVisible = descriptionVisible,
                onClick = onClick,
            )
        },
    )
}

@Composable
private fun CustomListContent(
    list: CustomList,
    likesVisible: Boolean,
    userVisible: Boolean,
    descriptionVisible: Boolean,
    onClick: () -> Unit,
) {
    val images = remember(list.images?.posters) {
        list.images?.getPostersUrl()?.take(8)
    }

    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        CustomListHeader(
            list = list,
            likesVisible = likesVisible,
            userVisible = userVisible,
            descriptionVisible = descriptionVisible,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (!images.isNullOrEmpty()) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .onClick(onClick = onClick)
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                val availableHeight = maxHeight
                val availableWidth = maxWidth
                val cardWidth = availableHeight * VerticalImageAspectRatio
                val imageCount = images.size

                val offset = when {
                    imageCount <= 1 -> 0.dp
                    imageCount <= 4 -> cardWidth * 0.66f
                    else -> (availableWidth - cardWidth) / (imageCount - 1)
                }

                images.forEachIndexed { index, url ->
                    VerticalMediaCard(
                        title = "",
                        imageUrl = url,
                        width = cardWidth,
                        corner = 12.dp,
                        enabled = false,
                        more = false,
                        modifier = Modifier
                            .padding(start = offset * index),
                    )
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    )
                    .background(
                        color = TraktTheme.colors.skeletonShimmer,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .fillMaxSize(),
            ) {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}

@Composable
private fun CustomListHeader(
    list: CustomList,
    likesVisible: Boolean,
    userVisible: Boolean,
    descriptionVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.weight(1F, false),
        ) {
            if (userVisible) {
                Box(
                    modifier = Modifier.size(36.dp),
                ) {
                    val avatarBorder = if (list.user.isAnyVip) TraktTheme.colors.vipAccent else Color.Transparent
                    val avatar = list.user.images?.avatar?.full
                    if (avatar != null) {
                        AsyncImage(
                            model = avatar,
                            contentDescription = "User avatar",
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_person_placeholder),
                            modifier =
                                Modifier
                                    .border(2.dp, avatarBorder, CircleShape)
                                    .clip(CircleShape),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_person_placeholder),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .border(2.dp, avatarBorder, CircleShape)
                                    .clip(CircleShape),
                        )
                    }
                }
            }

            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = list.name,
                    style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp, letterSpacing = 0.1.sp),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (descriptionVisible) {
                    if (!list.description.isNullOrBlank()) {
                        Text(
                            text = list.description ?: "",
                            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.text_by),
                            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = list.user.displayName,
                            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp, fontWeight = W500),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (likesVisible) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .onClick(onClick = { }),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_thumb_up2),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier
                        .size(16.dp),
                )
                list.likes?.let {
                    Text(
                        text = it.thousandsFormat(),
                        style = TraktTheme.typography.meta,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler =
            AsyncImagePreviewHandler {
                ColorImage(Color.LightGray.toArgb())
            }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                CustomListCard(
                    list = PreviewData.customList1.copy(
                        name = "A very long list name that should be truncated",
                        likes = 12341,
                    ),
                    likesVisible = true,
                    modifier = Modifier
                        .aspectRatio(HorizontalImageAspectRatio),
                    onClick = {},
                )

                CustomListCard(
                    list = PreviewData.customList1.copy(
                        type = Type.ALL,
                        images = Images(
                            posters = listOf(
                                "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                                "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                                "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                            ).toImmutableList(),
                        ),
                    ),
                    descriptionVisible = true,
                    modifier = Modifier
                        .aspectRatio(HorizontalImageAspectRatio),
                    onClick = {},
                )
            }
        }
    }
}
