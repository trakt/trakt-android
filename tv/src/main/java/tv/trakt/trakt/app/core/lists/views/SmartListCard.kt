package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime

private val cardShape = RoundedCornerShape(12.dp)
private val posterShape = RoundedCornerShape(8.dp)

@Composable
internal fun SmartListCard(
    list: SmartList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardDefaults.shape(shape = cardShape),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = (2.75).dp,
                    color = TraktTheme.colors.accent,
                ),
                shape = cardShape,
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = TraktTheme.colors.customListContainer,
            focusedContainerColor = TraktTheme.colors.customListContainer,
        ),
        scale = CardDefaults.scale(
            focusedScale = 1.02f,
        ),
    ) {
        SmartListContent(list = list)
    }
}

@Composable
private fun SmartListContent(list: SmartList) {
    val images = remember(list.images?.posters) {
        list.images?.getPostersUrl()?.take(8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        SmartListHeader(
            list = list,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (images.isNullOrEmpty()) {
            Text(
                text = stringResource(R.string.list_placeholder_empty),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            Box(
                modifier = Modifier.padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
            ) {
                images.forEachIndexed { index, url ->
                    PreviewPoster(
                        imageUrl = url,
                        modifier = Modifier.padding(start = (32 * index).dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1F))
    }
}

// Plain image instead of VerticalMediaCard so decorative posters never take D-pad focus.
@Composable
private fun PreviewPoster(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .width(70.dp)
            .aspectRatio(CardDefaults.VerticalImageAspectRatio)
            .clip(posterShape)
            .background(TraktTheme.colors.placeholderContainer),
    )
}

@Composable
private fun SmartListHeader(
    list: SmartList,
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

        Column(verticalArrangement = spacedBy(2.dp)) {
            Text(
                text = list.name,
                style = TraktTheme.typography.paragraph,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = list.rememberDescription(),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(verticalArrangement = spacedBy(16.dp)) {
                val list = SmartList(
                    ids = Ids(trakt = TraktId(1), slug = SlugId("smart")),
                    name = "Unwatched sci-fi from the 2010s",
                    privacy = null,
                    createdAt = ZonedDateTime.now(),
                    updatedAt = ZonedDateTime.now(),
                    images = Images(
                        posters = persistentListOf("poster1", "poster2", "poster3"),
                    ),
                    filters = SmartListFilters(
                        media = MediaMode.Movies,
                        source = SmartListSource.Popular,
                        genres = persistentListOf(MediaGenre.ScienceFiction),
                        years = persistentListOf(2010, 2019),
                        ignoreWatched = true,
                    ),
                )

                SmartListCard(
                    list = list,
                    onClick = {},
                    modifier = Modifier
                        .height(TraktTheme.size.detailsCustomListSize)
                        .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                )

                SmartListCard(
                    list = list.copy(images = null),
                    onClick = {},
                    modifier = Modifier
                        .height(TraktTheme.size.detailsCustomListSize)
                        .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                )
            }
        }
    }
}
