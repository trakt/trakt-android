package tv.trakt.trakt.core.discover.sections.releases.views

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.chips.NewChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieReleasesItemView(
    item: CalendarItem.MovieItem,
    modifier: Modifier = Modifier,
    onClick: (TraktId) -> Unit = { },
    onLongClick: () -> Unit = {},
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.movie.images?.getFanartUrl(),
        onClick = { onClick(item.movie.ids.trakt) },
        onLongClick = onLongClick,
        cardContent = {
            Row(
                horizontalArrangement = spacedBy(3.dp),
                verticalAlignment = CenterVertically,
            ) {
                if (item.movie.isNew) {
                    NewChip()
                }

                item.releasedAt?.let { releasedAt ->
                    InfoChip(
                        text = releasedAt.toLocal().relativeDateTimeString(),
                        iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                        containerColor = TraktTheme.colors.chipContainerOnContent,
                    )
                }
            }
        },
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.movie.title,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

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
        Column(
            verticalArrangement = spacedBy(16.dp),
        ) {
            listOf(
                PreviewData.newMovie,
                PreviewData.movie1.copy(released = PreviewData.movie1.released?.minusDays(30)),
            ).forEach { movie ->
                MovieReleasesItemView(
                    item = CalendarItem.MovieItem(
                        watched = false,
                        movie = movie,
                    ),
                )
            }
        }
    }
}
