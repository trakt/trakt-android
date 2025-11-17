package tv.trakt.trakt.core.lists.sections.personal.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.temporal.ChronoUnit.DAYS

@Composable
internal fun ListsPersonalItemView(
    item: PersonalListItem,
    modifier: Modifier = Modifier,
    showMediaIcon: Boolean = false,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onMovieClick: (Movie) -> Unit = { },
    onShowClick: (Show) -> Unit = { },
    onLongClick: () -> Unit = { },
) {
    when (item) {
        is PersonalListItem.ShowItem -> {
            val isReleased = remember(item.show.released) {
                item.show.released?.isBefore(nowUtc()) ?: false
            }
            VerticalMediaCard(
                title = item.show.title,
                watched = watched,
                watchlist = watchlist,
                imageUrl = item.images?.getPosterUrl(),
                onClick = { onShowClick(item.show) },
                onLongClick = onLongClick,
                chipSpacing = 10.dp,
                chipContent = { modifier ->
                    if (isReleased) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(4.dp),
                            modifier = modifier,
                        ) {
                            if (showMediaIcon) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_shows_off),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.chipContent,
                                    modifier = Modifier
                                        .size(13.dp),
                                )
                            }

                            val airedEpisodes = stringResource(
                                R.string.tag_text_number_of_episodes,
                                item.show.airedEpisodes,
                            )

                            val footerText = remember {
                                buildString {
                                    item.show.released?.let {
                                        append(it.year.toString())
                                    } ?: append("TBA")

                                    if (item.show.airedEpisodes > 0) {
                                        append(" • ")
                                        append(airedEpisodes)
                                    }
                                }
                            }

                            Text(
                                text = footerText,
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(4.dp),
                            modifier = modifier,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar_upcoming),
                                contentDescription = null,
                                tint = TraktTheme.colors.chipContent,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = item.show.released?.relativeDateTimeString() ?: "",
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                modifier = modifier,
            )
        }
        is PersonalListItem.MovieItem -> {
            val isReleased = remember(item.movie.released) {
                item.movie.released?.isTodayOrBefore() ?: false
            }
            VerticalMediaCard(
                title = item.movie.title,
                imageUrl = item.images?.getPosterUrl(),
                watched = watched,
                watchlist = watchlist,
                onClick = { onMovieClick(item.movie) },
                onLongClick = onLongClick,
                chipSpacing = 10.dp,
                chipContent = { modifier ->
                    if (isReleased) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(4.dp),
                            modifier = modifier,
                        ) {
                            if (showMediaIcon) {
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
                            }

                            Text(
                                text = remember {
                                    val runtime = item.movie.runtime?.inWholeMinutes
                                    if (runtime != null) {
                                        "${item.movie.year} • ${runtime.durationFormat()}"
                                    } else {
                                        item.movie.year.toString()
                                    }
                                },
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(4.dp),
                            modifier = modifier,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar_upcoming),
                                contentDescription = null,
                                tint = TraktTheme.colors.chipContent,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = item.movie.released?.relativeDateString() ?: "",
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
        ListsPersonalItemView(
            item = PersonalListItem.ShowItem(
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
        ListsPersonalItemView(
            item = PersonalListItem.MovieItem(
                movie = PreviewData.movie1,
                listedAt = nowUtcInstant().minus(3, DAYS),
            ),
        )
    }
}
