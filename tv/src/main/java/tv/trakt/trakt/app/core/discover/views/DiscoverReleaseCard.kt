package tv.trakt.trakt.app.core.discover.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.chips.EpisodeStatusChip
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.rememberEpisodeStatus
import tv.trakt.trakt.resources.R

@Composable
internal fun DiscoverReleaseCard(
    item: CalendarItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.images?.getFanartUrl(),
        onClick = onClick,
        cardContent = {
            Column(
                verticalArrangement = spacedBy(2.dp),
            ) {
                if (item is CalendarItem.EpisodeItem) {
                    item.episodes
                        .rememberEpisodeStatus()
                        ?.let { EpisodeStatusChip(status = it) }
                }

                InfoChip(
                    text = item.releasedAt?.toLocal()?.relativeDateTimeString() ?: "TBA",
                    iconPainter = when {
                        item.isReleased -> painterResource(R.drawable.ic_calendar_check)
                        else -> painterResource(R.drawable.ic_calendar_upcoming)
                    },
                    containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                )
            }
        },
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = when (item) {
                        is CalendarItem.EpisodeItem -> item.show.title
                        is CalendarItem.MovieItem -> item.title
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = item.subtitle(),
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

private val CalendarItem.isReleased: Boolean
    get() = when (this) {
        is CalendarItem.EpisodeItem -> episode.isReleased
        is CalendarItem.MovieItem -> movie.isReleased
    }

@Composable
private fun CalendarItem.subtitle(): String {
    return when (this) {
        is CalendarItem.MovieItem -> when (movie.runtime) {
            null -> stringResource(R.string.translated_value_type_movie)
            else -> rememberDurationFormat(movie.runtime?.inWholeMinutes)
        }

        is CalendarItem.EpisodeItem -> when {
            isFullSeason -> stringResource(R.string.text_season_number, episode.season)
            episodes.size > 1 -> stringResource(
                R.string.episode_footer_season_episode_range,
                episode.season,
                episodes.first().number,
                episodes.last().number,
            )
            else -> episode.seasonEpisodeString()
        }
    }
}
