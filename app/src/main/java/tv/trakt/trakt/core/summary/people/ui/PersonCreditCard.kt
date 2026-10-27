package tv.trakt.trakt.core.summary.people.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.uppercaseWords
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PersonCreditCard(
    item: PersonCreditItem,
    userCollection: UserCollectionState,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
) {
    val badges = CreditBadges(
        watched = userCollection.isWatched(item.id, item.type, item.airedEpisodes),
        watching = userCollection.isWatching(item.id, item.type, item.airedEpisodes),
        watchlist = userCollection.isWatchlist(item.id, item.type),
    )

    when (item) {
        is PersonCreditItem.ShowItem -> ShowCreditCard(
            item = item,
            badges = badges,
            onShowClick = onShowClick,
            onShowLongClick = onShowLongClick,
            modifier = modifier,
        )

        is PersonCreditItem.MovieItem -> MovieCreditCard(
            item = item,
            badges = badges,
            onMovieClick = onMovieClick,
            onMovieLongClick = onMovieLongClick,
            modifier = modifier,
        )
    }
}

private data class CreditBadges(
    val watched: Boolean,
    val watching: Boolean,
    val watchlist: Boolean,
)

@Composable
private fun ShowCreditCard(
    item: PersonCreditItem.ShowItem,
    badges: CreditBadges,
    onShowClick: (Show) -> Unit,
    onShowLongClick: (Show) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        watched = badges.watched,
        watching = badges.watching,
        watchlist = badges.watchlist,
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = modifier,
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier
                            .size(13.dp),
                    )

                    val episodesCount = stringResource(
                        R.string.tag_text_number_of_episodes,
                        item.episodesCount,
                    )

                    val tbaLabel = stringResource(R.string.tag_text_tba)

                    val footerText = remember {
                        buildString {
                            item.show.releasedAt?.let {
                                append(it.toLocal().year.toString())
                            } ?: append(tbaLabel)

                            if (item.episodesCount > 0) {
                                append(" • ")
                                append(episodesCount)
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

                Text(
                    text = (item.credit ?: "")
                        .ifBlank { stringResource(R.string.text_unknown) }
                        .uppercaseWords(),
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = { onShowClick(item.show) },
        onLongClick = { onShowLongClick(item.show) },
        modifier = modifier,
    )
}

@Composable
private fun MovieCreditCard(
    item: PersonCreditItem.MovieItem,
    badges: CreditBadges,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        watched = badges.watched,
        watching = badges.watching,
        watchlist = badges.watchlist,
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = modifier,
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
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

                    val footerRuntime = rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
                    Text(
                        text = remember {
                            val runtime = item.movie.runtime?.inWholeMinutes
                            if (runtime != null) {
                                "${item.movie.year} • $footerRuntime"
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

                Text(
                    text = (item.credit ?: "")
                        .ifBlank { stringResource(R.string.text_unknown) }
                        .uppercaseWords(),
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = { onMovieClick(item.movie) },
        onLongClick = { onMovieLongClick(item.movie) },
        modifier = modifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
        ) {
            PersonCreditCard(
                item = PersonCreditItem.ShowItem(
                    show = PreviewData.show1,
                    episodesCount = 12,
                    credit = "Self",
                ),
                userCollection = UserCollectionState.Default,
            )
            PersonCreditCard(
                item = PersonCreditItem.MovieItem(
                    movie = PreviewData.movie1,
                    credit = "Director",
                ),
                userCollection = UserCollectionState.Default,
            )
        }
    }
}
