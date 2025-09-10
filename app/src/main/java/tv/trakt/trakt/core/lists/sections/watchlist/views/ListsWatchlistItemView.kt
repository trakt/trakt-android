package tv.trakt.trakt.core.lists.sections.watchlist.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.temporal.ChronoUnit.DAYS

@Composable
internal fun ListsWatchlistItemView(
    item: WatchlistItem,
    modifier: Modifier = Modifier,
    showMediaIcon: Boolean = false,
) {
    when (item) {
        is WatchlistItem.ShowItem -> {
            val isReleased = remember(item.show.released) {
                item.show.released?.isBefore(nowUtc()) ?: false
            }
            VerticalMediaCard(
                title = item.show.title,
                imageUrl = item.images?.getPosterUrl(),
                chipContent = {
                    if (isReleased) {
                        Row(
                            horizontalArrangement = spacedBy(5.dp),
                        ) {
                            item.show.released?.let {
                                InfoChip(
                                    text = it.year.toString(),
                                    iconPainter = when {
                                        showMediaIcon -> painterResource(R.drawable.ic_shows_off)
                                        else -> null
                                    },
                                    iconPadding = 2.dp,
                                )
                            }
                            if (item.show.airedEpisodes > 0) {
                                InfoChip(
                                    text = stringResource(
                                        R.string.tag_text_number_of_episodes,
                                        item.show.airedEpisodes,
                                    ),
                                )
                            }
                        }
                    } else {
                        InfoChip(
                            text = item.show.released?.relativeDateTimeString() ?: "",
                            iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                            iconPadding = 1.dp,
                        )
                    }
                },
                modifier = modifier,
            )
        }
        is WatchlistItem.MovieItem -> {
            val isReleased = remember(item.movie.released) {
                item.movie.released?.isTodayOrBefore() ?: false
            }
            VerticalMediaCard(
                title = item.movie.title,
                imageUrl = item.images?.getPosterUrl(),
                chipContent = {
                    if (isReleased) {
                        Row(
                            horizontalArrangement = spacedBy(5.dp),
                        ) {
                            item.movie.released?.let {
                                InfoChip(
                                    text = it.year.toString(),
                                    iconPainter = when {
                                        showMediaIcon -> painterResource(R.drawable.ic_movies_off)
                                        else -> null
                                    },
                                )
                            }
                            item.movie.runtime?.inWholeMinutes?.let {
                                val runtimeString = remember(item.movie.runtime) {
                                    it.durationFormat()
                                }
                                InfoChip(
                                    text = runtimeString,
                                )
                            }
                        }
                    } else {
                        InfoChip(
                            text = item.movie.released?.relativeDateString() ?: "",
                            iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                            iconPadding = 1.dp,
                        )
                    }
                },
                modifier = modifier,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewShow() {
    TraktTheme {
        ListsWatchlistItemView(
            item = WatchlistItem.ShowItem(
                show = PreviewData.show1,
                listedAt = nowUtcInstant().minus(3, DAYS),
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewMovie() {
    TraktTheme {
        ListsWatchlistItemView(
            item = WatchlistItem.MovieItem(
                movie = PreviewData.movie1,
                listedAt = nowUtcInstant().minus(3, DAYS),
            ),
        )
    }
}
