package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.EpisodeItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.MovieItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.SeasonItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.ShowItem
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.resources.R

@Composable
internal fun ListMediaItemCard(
    item: ListMediaItem,
    collection: UserCollectionState,
    sorting: Sorting,
    enabled: Boolean,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalMediaCard(
        title = item.title,
        imageUrl = item.images?.getPosterUrl(),
        watched = when (item) {
            is ShowItem -> collection.isWatched(item.id, MediaType.Show, item.show.airedEpisodes)
            is MovieItem -> collection.isWatched(item.id, MediaType.Movie, null)
            is EpisodeItem -> collection.isEpisodeWatched(item.id)
            is SeasonItem -> false
        },
        watching = when (item) {
            is ShowItem -> collection.isWatching(item.id, MediaType.Show, item.show.airedEpisodes)
            is MovieItem, is SeasonItem, is EpisodeItem -> false
        },
        watchlist = when (item) {
            is ShowItem -> collection.isWatchlist(item.id, MediaType.Show)
            is MovieItem -> collection.isWatchlist(item.id, MediaType.Movie)
            is SeasonItem, is EpisodeItem -> false
        },
        onClick = {
            if (!enabled) return@VerticalMediaCard
            when (item) {
                is ShowItem -> onShowClick(item.id)
                is MovieItem -> onMovieClick(item.id)
                is SeasonItem -> onShowClick(item.show.ids.trakt)
                is EpisodeItem -> onEpisodeClick(item.show.ids.trakt, item.episode)
            }
        },
        chipContent = {
            ListSortSubtitle(
                sorting = sorting,
                values = item.sortValues,
            ) {
                ItemSubtitle(item)
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ItemSubtitle(item: ListMediaItem) {
    val iconRes = when (item) {
        is MovieItem -> R.drawable.ic_movies_off
        is ShowItem, is SeasonItem, is EpisodeItem -> R.drawable.ic_shows_off
    }

    val text = when (item) {
        is ShowItem -> {
            val episodes = item.show.airedEpisodes.takeIf { it > 0 }
                ?.let { stringResource(R.string.tag_text_number_of_episodes, it) }
            listOfNotNull(item.show.year?.toString(), episodes)
                .joinToString("  •  ")
        }

        is MovieItem -> {
            item.movie.yearString +
                "  •  ${rememberDurationFormat(item.movie.runtime?.inWholeMinutes)}"
        }

        is SeasonItem -> {
            when (item.season.number) {
                0 -> stringResource(R.string.text_season_specials)
                else -> stringResource(R.string.text_season_number, item.season.number)
            }
        }

        is EpisodeItem -> {
            item.episode.seasonEpisodeString()
        }
    }

    if (text.isEmpty()) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer {
                    translationY = -0.5.dp.toPx()
                },
        )

        Text(
            text = text,
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Row(horizontalArrangement = spacedBy(16.dp)) {
            listOf(
                ShowItem(show = PreviewData.show1),
                MovieItem(movie = PreviewData.movie1),
                SeasonItem(show = PreviewData.show1, season = PreviewData.season1),
                EpisodeItem(show = PreviewData.show1, episode = PreviewData.episode1),
            ).forEach {
                ListMediaItemCard(
                    item = it,
                    collection = UserCollectionState.Default,
                    sorting = Sorting.Default,
                    enabled = true,
                    onShowClick = {},
                    onMovieClick = {},
                    onEpisodeClick = { _, _ -> },
                    modifier = Modifier.size(width = 120.dp, height = 220.dp),
                )
            }
        }
    }
}
