package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_GENRES = 2
private const val OVERVIEW_LINES = 8
private const val HEADER_RATIO = 16F / 10F

/** Back face of the flipped story poster. */
@Composable
internal fun TodayPosterDetails(
    media: TodayMedia,
    modifier: Modifier = Modifier,
) {
    val cardBackground = TraktTheme.colors.commentContainer

    Column(
        modifier = modifier.background(cardBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(HEADER_RATIO),
        ) {
            AsyncImage(
                model = media.images?.getFanartUrl(Images.Size.MEDIUM),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.3F to Color.Transparent,
                            1F to cardBackground,
                        ),
                    ),
            )
            Column(
                verticalArrangement = spacedBy(2.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(
                    text = media.title,
                    style = TraktTheme.typography.heading5,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = mediaMeta(media),
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        media.overview?.let {
            Text(
                text = it,
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
                maxLines = OVERVIEW_LINES,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun mediaMeta(media: TodayMedia): String {
    val genres = media.genres
        .take(MAX_GENRES)
        .map { stringResource(it.displayStringRes) }

    return (listOfNotNull(media.year?.toString()) + genres)
        .joinToString(" · ")
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TodayPosterDetails(
            media = TodayMedia.ShowMedia(PreviewData.show1),
            modifier = Modifier.width(240.dp).aspectRatio(2F / 3F),
        )
    }
}
