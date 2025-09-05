package tv.trakt.trakt.core.home.sections.activity

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.episodes.model.Episode
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.PERSONAL
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.SOCIAL
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.EpisodeSocialItemView
import tv.trakt.trakt.core.home.sections.activity.views.MovieSocialItemView
import tv.trakt.trakt.core.home.views.HomeEmptySocialView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeActivityView(
    modifier: Modifier = Modifier,
    viewModel: HomeActivityViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onNavigateToMovie: (movieId: TraktId) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeActivityContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onEpisodeClick = onNavigateToEpisode,
        onMovieClick = onNavigateToMovie,
        onFilterClick = viewModel::setFilter,
    )
}

@Composable
internal fun HomeActivityContent(
    state: HomeActivityState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onEpisodeClick: (TraktId, Episode) -> Unit = { _, _ -> },
    onMovieClick: (TraktId) -> Unit = {},
    onFilterClick: (HomeActivityFilter) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
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
                text = stringResource(R.string.list_title_activity),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            if (!state.items.isNullOrEmpty() || state.loading != DONE) {
                Text(
                    text = stringResource(R.string.button_text_view_all),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.buttonSecondary,
                )
            }
        }

        if (!state.items.isNullOrEmpty() || state.loading.isLoading || state.user != null) {
            ContentFilters(
                state = state,
                headerPadding = headerPadding,
                onFilterClick = onFilterClick
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    when {
                        state.error != null -> {
                            Text(
                                text =
                                    "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                        state.items?.isEmpty() == true -> {
                            HomeEmptySocialView(
                                modifier = Modifier
                                    .padding(contentPadding),
                            )
                        }
                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onEpisodeClick = onEpisodeClick,
                                onMovieClick = onMovieClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentFilters(
    headerPadding: PaddingValues,
    state: HomeActivityState,
    onFilterClick: (HomeActivityFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(headerPadding)
            .padding(
                top = 12.dp,
                bottom = 14.dp,
            ),
        horizontalArrangement = spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = state.filter == SOCIAL,
            text = stringResource(SOCIAL.displayRes),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            onClick = { onFilterClick(SOCIAL) },
        )

        FilterChip(
            selected = state.filter == PERSONAL,
            text = stringResource(PERSONAL.displayRes),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            onClick = { onFilterClick(PERSONAL) },
        )
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<HomeActivityItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onMovieClick: (TraktId) -> Unit,
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
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.id },
        ) { item ->
            when (item) {
                is HomeActivityItem.MovieItem ->
                    MovieSocialItemView(
                        item = item,
                        onClick = onMovieClick,
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
                is HomeActivityItem.EpisodeItem ->
                    EpisodeSocialItemView(
                        item = item,
                        onClick = onEpisodeClick,
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
            }
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeActivityContent(
            state = HomeActivityState(
                loading = IDLE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        HomeActivityContent(
            state = HomeActivityState(
                loading = LOADING,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        HomeActivityContent(
            state = HomeActivityState(
                loading = DONE,
                items = emptyList<HomeActivityItem>().toImmutableList(),
            ),
        )
    }
}
