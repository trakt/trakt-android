package tv.trakt.trakt.ui.components.mediacards.list

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio
import java.time.ZonedDateTime

@Composable
internal fun SmartListCard(
    smartList: SmartList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = DefaultCardShape,
        colors = cardColors(
            containerColor = TraktTheme.colors.customListContainer,
        ),
        content = {
            SmartListContent(
                smartList = smartList,
                onClick = onClick,
                onDeleteClick = onDeleteClick,
            )
        },
    )
}

@Composable
private fun SmartListContent(
    smartList: SmartList,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val images = remember(smartList.images?.posters) {
        smartList.images?.getPostersUrl()?.take(8)
    }

    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = TraktTheme.colors.customListContainer,
                shape = DefaultCardShape,
            )
            .padding(vertical = 16.dp),
    ) {
        SmartListHeader(
            list = smartList,
            onDeleteClick = onDeleteClick,
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
                val cardWidth = maxHeight * VerticalImageAspectRatio
                val imageCount = images.size

                val offset = when {
                    imageCount <= 1 -> 0.dp
                    imageCount <= 4 -> cardWidth * 0.66f
                    else -> (maxWidth - cardWidth) / (imageCount - 1)
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
                            .zIndex((imageCount - index).toFloat())
                            .padding(start = offset * index),
                    )
                }
            }
        } else {
            EmptyContent()
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = RoundedCornerShape(17.dp),
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

@Composable
private fun SmartListHeader(
    list: SmartList,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = TraktTheme.colors.chipContainer,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                painter = painterResource(list.filters.media.offIcon),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(1.dp),
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        ) {
            Text(
                text = list.name,
                style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp, letterSpacing = 0.1.sp),
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = list.rememberDescription(),
                style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        SmartListDropdown(
            onDeleteClick = onDeleteClick,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            SmartListCard(
                smartList = SmartList(
                    ids = Ids(trakt = TraktId(1), slug = SlugId("smart")),
                    name = "Unwatched sci-fi from the 2010s",
                    privacy = null,
                    createdAt = ZonedDateTime.now(),
                    updatedAt = ZonedDateTime.now(),
                    images = Images(
                        posters = persistentListOf(
                            "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                            "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                            "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                        ),
                    ),
                    filters = SmartListFilters(
                        source = SmartListSource.Popular,
                        media = MediaMode.Movies,
                        genres = persistentListOf(MediaGenre.ScienceFiction),
                        subgenres = EmptyImmutableList,
                        certifications = EmptyImmutableList,
                        languages = EmptyImmutableList,
                        countries = persistentListOf("gb", "fr", "de", "it"),
                        statuses = EmptyImmutableList,
                        networks = EmptyImmutableList,
                        availability = persistentListOf(GlobalFilter.Availability.AllDigitalReleases),
                        years = persistentListOf(2010, 2019),
                        ratings = persistentListOf(70, 100),
                        runtimes = EmptyImmutableList,
                        imdbRatings = EmptyImmutableList,
                        rtMeters = EmptyImmutableList,
                        rtUserMeters = EmptyImmutableList,
                        ignoreWatched = true,
                        ignoreWatchlisted = false,
                    ),
                ),
                onClick = {},
                modifier = Modifier.aspectRatio(HorizontalImageAspectRatio),
            )
        }
    }
}
