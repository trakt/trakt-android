package tv.trakt.trakt.core.profile.sections.favorites.views

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
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.temporal.ChronoUnit.DAYS

@Composable
internal fun FavoriteItemView(
    item: FavoriteItem,
    modifier: Modifier = Modifier,
    showMediaIcon: Boolean = false,
    showMore: Boolean = false,
    watched: Boolean = false,
    watching: Boolean = false,
    watchlist: Boolean = false,
    onShowClick: () -> Unit = {},
    onMovieClick: () -> Unit = {},
    onShowLongClick: () -> Unit = {},
    onMovieLongClick: () -> Unit = {},
) {
    when (item) {
        is FavoriteItem.ShowItem -> {
            val isReleased = item.show.rememberReleased()
            VerticalMediaCard(
                title = item.show.title,
                imageUrl = item.images?.getPosterUrl(),
                more = showMore,
                watched = watched,
                watching = watching,
                watchlist = watchlist,
                onClick = onShowClick,
                onLongClick = onShowLongClick,
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
                                    item.released?.let {
                                        append(it.toLocal().year.toString())
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
                                text = item.show.releasedAt?.toLocal()?.relativeDateTimeString() ?: "",
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

        is FavoriteItem.MovieItem -> {
            val isReleased = remember(item.movie.released) {
                item.movie.isReleased
            }

            VerticalMediaCard(
                title = item.movie.title,
                imageUrl = item.images?.getPosterUrl(),
                onClick = onMovieClick,
                more = showMore,
                watched = watched,
                watching = watching,
                watchlist = watchlist,
                onLongClick = onMovieLongClick,
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

                            val runtime = rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
                            Text(
                                text = remember {
                                    if (runtime != "N/A") {
                                        "${item.movie.year} • $runtime"
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
                                text = item.movie.released?.relativeDateString() ?: "TBA",
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
private fun Preview1() {
    TraktTheme {
        FavoriteItemView(
            item = FavoriteItem.ShowItem(
                show = PreviewData.show1,
                rank = 0,
                listedAt = nowUtcInstant().minus(3, DAYS),
            ),
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(8.dp),
        ) {
            FavoriteItemView(
                item = FavoriteItem.MovieItem(
                    movie = PreviewData.movie1,
                    rank = 0,
                    listedAt = nowUtcInstant().minus(3, DAYS),
                ),
            )

            FavoriteItemView(
                item = FavoriteItem.ShowItem(
                    show = PreviewData.show1.copy(
                        releasedAt = nowUtcInstant().minus(5, DAYS),
                    ),
                    rank = 0,
                    listedAt = nowUtcInstant().minus(3, DAYS),
                ),
            )
        }
    }
}
