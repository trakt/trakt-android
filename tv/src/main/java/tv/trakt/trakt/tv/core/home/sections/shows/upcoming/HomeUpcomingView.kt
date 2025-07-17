package tv.trakt.trakt.tv.core.home.sections.shows.upcoming

import InfoChip
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.home.sections.shows.upcoming.model.CalendarShow
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.tv.helpers.extensions.toLocal
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun HomeUpcomingView(
    modifier: Modifier = Modifier,
    viewModel: HomeUpcomingViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Show) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    HomeUpcomingContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFocused = onFocused,
        onNavigateToEpisode = onNavigateToEpisode,
    )
}

@Composable
internal fun HomeUpcomingContent(
    state: HomeUpcomingState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Show) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.header_shows_upcoming),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                )
            }

            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.info_generic_empty_list),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier
                        .padding(headerPadding),
                )
            }

            else -> {
                ContentList(
                    listItems = { state.items ?: emptyList<CalendarShow>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = { show, episode ->
                        onNavigateToEpisode(show.ids.trakt, episode)
                    },
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<CalendarShow>,
    onFocused: (Show) -> Unit,
    onClick: (Show, Episode) -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems(),
            key = { it.episode.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
                onFocused = onFocused,
                onClick = onClick,
            )
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentListItem(
    item: CalendarShow,
    onFocused: (Show) -> Unit,
    onClick: (Show, Episode) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl =
            item.episode.images?.getScreenshotUrl()
                ?: item.show.images?.getFanartUrl(),
        onClick = {
            onClick(item.show, item.episode)
        },
        cardContent = {
            val dateString = remember(item.releaseAt) {
                item.releaseAt.toLocal().relativeDateTimeString()
            }
            InfoChip(
                text = dateString,
                iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                modifier = Modifier.padding(end = 8.dp),
            )
        },
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.show.title,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = when {
                    item.isFullSeason -> stringResource(
                        R.string.season_number,
                        item.episode.season,
                    )

                    else -> item.episode.seasonEpisodeString
                }

                Text(
                    text = subtitle,
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = Modifier
            .onFocusChanged {
                if (it.isFocused) onFocused(item.show)
            },
    )
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeUpcomingContent(
            state = HomeUpcomingState(),
        )
    }
}
