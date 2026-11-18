package tv.trakt.trakt.app.core.discover.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.resources.R

@Composable
internal fun DiscoverItemCard(
    item: DiscoverItem,
    section: DiscoverMediaSection,
    collection: UserCollectionState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.title,
        imageUrl = item.images?.getPosterUrl(),
        watched = collection.isWatched(item.id, item.type, item.airedEpisodes),
        watching = collection.isWatching(item.id, item.type, item.airedEpisodes),
        watchlist = collection.isWatchlist(item.id, item.type),
        onClick = onClick,
        chipContent = {
            when (section) {
                DiscoverMediaSection.Trending -> CountChip(
                    iconRes = R.drawable.ic_person_double,
                    iconSize = 12.dp,
                    iconSpacing = 5.dp,
                    count = item.count,
                )

                DiscoverMediaSection.Anticipated -> CountChip(
                    iconRes = R.drawable.ic_bookmark_off,
                    iconSize = 16.dp,
                    iconSpacing = 2.dp,
                    count = item.count,
                )

                DiscoverMediaSection.Popular -> PopularChip(item)
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CountChip(
    iconRes: Int,
    iconSize: Dp,
    iconSpacing: Dp,
    count: Int,
) {
    Column(
        verticalArrangement = spacedBy(1.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(iconSpacing),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = rememberThousandsFormat(count),
                style = TraktTheme.typography.cardTitle,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PopularChip(item: DiscoverItem) {
    val text = when (item) {
        is DiscoverItem.ShowItem -> {
            val episodes = stringResource(R.string.tag_text_number_of_episodes, item.show.airedEpisodes)
            item.show.year?.let { "$it  •  $episodes" } ?: episodes
        }

        is DiscoverItem.MovieItem -> {
            "${item.movie.released?.year ?: item.movie.year}" +
                "  •  ${rememberDurationFormat(item.movie.runtime?.inWholeMinutes)}"
        }
    }

    Column(
        verticalArrangement = spacedBy(1.dp),
    ) {
        Text(
            text = text,
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
