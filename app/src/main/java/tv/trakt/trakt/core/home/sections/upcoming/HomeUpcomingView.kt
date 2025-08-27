package tv.trakt.trakt.core.home.sections.upcoming

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.episodes.model.Episode
import tv.trakt.trakt.core.home.sections.upcoming.model.CalendarShow
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeUpcomingView(
    modifier: Modifier = Modifier,
    viewModel: HomeUpcomingViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onNavigateToEpisode: (Show, Episode) -> Unit = { _, _ -> },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeUpcomingContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onNavigateToEpisode = onNavigateToEpisode,
    )
}

@Composable
internal fun HomeUpcomingContent(
    state: HomeUpcomingState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToEpisode: (Show, Episode) -> Unit = { _, _ -> },
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.list_title_upcoming_schedule),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
        }

        Crossfade(
            targetState = state.isLoading,
            animationSpec = tween(200),
        ) { loading ->
            when {
                loading -> {
                    ContentLoadingList(
                        contentPadding = contentPadding,
                    )
                }

                state.items?.isEmpty() == true -> {
                    Text(
                        text = stringResource(R.string.list_placeholder_empty),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.heading6,
                        modifier = Modifier.padding(contentPadding),
                    )
                }

                else -> {
                    ContentList(
                        listItems = state.items?.toImmutableList() 
                            ?: emptyList<CalendarShow>().toImmutableList(),
                        contentPadding = contentPadding,
                        onNavigateToEpisode = onNavigateToEpisode,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingList(
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(count = 6) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<CalendarShow>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onNavigateToEpisode: (Show, Episode) -> Unit,
) {
    val currentList = remember { mutableIntStateOf(listItems.hashCode()) }

    LaunchedEffect(listItems) {
        val hashCode = listItems.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.episode.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
                onNavigateToEpisode = onNavigateToEpisode,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: CalendarShow,
    modifier: Modifier = Modifier,
    onNavigateToEpisode: (Show, Episode) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.episode.images?.getScreenshotUrl()
            ?: item.show.images?.getFanartUrl(),
        onClick = {
            onNavigateToEpisode(item.show, item.episode)
        },
        cardContent = {
            val dateString = remember(item.releaseAt) {
                item.releaseAt.toLocal().relativeDateTimeString()
            }
            InfoChip(
                text = dateString,
                containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.75F),
            )
        },
        footerContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
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
                        R.string.text_season_number,
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
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        HomeUpcomingContent(
            state = HomeUpcomingState(isLoading = false, items = null),
        )
    }
}
