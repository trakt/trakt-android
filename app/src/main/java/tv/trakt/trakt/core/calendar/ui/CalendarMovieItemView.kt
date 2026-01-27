package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun CalendarMovieItemView(
    item: HomeUpcomingItem.MovieItem,
    modifier: Modifier = Modifier,
    onClick: (TraktId) -> Unit = { },
) {
    HorizontalMediaCard(
        title = "",
        more = false,
        containerImageUrl = item.movie.images?.getFanartUrl(),
        onClick = { onClick(item.movie.ids.trakt) },
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Row(
                    horizontalArrangement = spacedBy(3.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movies_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer {
                                translationY = -(0.25).dp.toPx()
                            },
                    )

                    Text(
                        text = item.movie.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = stringResource(R.string.translated_value_type_movie),
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarMovieItemView(
            item = HomeUpcomingItem.MovieItem(
                id = 1.toTraktId(),
                releasedAt = Instant.now(),
                movie = PreviewData.movie1,
            ),
        )
    }
}
