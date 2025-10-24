@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.user.features.profile.sections.history

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.sheets.HomeActivityItemSheet
import tv.trakt.trakt.core.home.sections.activity.views.EpisodeSocialItemView
import tv.trakt.trakt.core.home.sections.activity.views.MovieSocialItemView
import tv.trakt.trakt.core.home.views.HomeEmptySocialView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileHistoryView(
    modifier: Modifier = Modifier,
    viewModel: ProfileHistoryViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onMoreClick: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
    }

    var contextSheet by remember { mutableStateOf<HomeActivityItem?>(null) }

    ProfileHistoryContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onShowClick = {
            viewModel.navigateToShow(it.show)
        },
        onEpisodeClick = {
            viewModel.navigateToEpisode(
                show = it.show,
                episode = it.episode,
            )
        },
        onEpisodeLongClick = {
            contextSheet = it
        },
        onMovieClick = {
            viewModel.navigateToMovie(it)
        },
        onMovieLongClick = {
            contextSheet = it
        },
        onMoreClick = {
            if (state.loading.isLoading) {
                return@ProfileHistoryContent
            }
            onMoreClick()
        },
    )

    HomeActivityItemSheet(
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onPlayRemoved = {
            viewModel.loadData(ignoreErrors = true)
        },
    )
}

@Composable
internal fun ProfileHistoryContent(
    state: ProfileHistoryState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onShowClick: (HomeActivityItem.EpisodeItem) -> Unit = {},
    onEpisodeClick: (HomeActivityItem.EpisodeItem) -> Unit = {},
    onEpisodeLongClick: (HomeActivityItem.EpisodeItem) -> Unit = {},
    onMovieClick: (Movie) -> Unit = { },
    onMovieLongClick: (HomeActivityItem.MovieItem) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onMoreClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_recently_watched),
            )

            if (!state.items.isNullOrEmpty() || state.loading != DONE) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationX = (4.9).dp.toPx()
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(TraktTheme.spacing.mainRowHeaderSpace))

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
                                maxLines = 10,
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
                                onShowClick = onShowClick,
                                onEpisodeClick = onEpisodeClick,
                                onEpisodeLongClick = onEpisodeLongClick,
                                onMovieClick = onMovieClick,
                                onMovieLongClick = onMovieLongClick,
                            )
                        }
                    }
                }
            }
        }
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
    onShowClick: (HomeActivityItem.EpisodeItem) -> Unit,
    onEpisodeClick: (HomeActivityItem.EpisodeItem) -> Unit,
    onEpisodeLongClick: (HomeActivityItem.EpisodeItem) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (HomeActivityItem.MovieItem) -> Unit,
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
                        onClick = { onMovieClick(item.movie) },
                        onLongClick = { onMovieLongClick(item) },
                        moreButton = true,
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                is HomeActivityItem.EpisodeItem ->
                    EpisodeSocialItemView(
                        item = item,
                        onClick = { onEpisodeClick(item) },
                        onShowClick = { onShowClick(item) },
                        onLongClick = { onEpisodeLongClick(item) },
                        moreButton = true,
                        modifier = Modifier
                            .animateItem(
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
        ProfileHistoryContent(
            state = ProfileHistoryState(
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
        ProfileHistoryContent(
            state = ProfileHistoryState(
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
        ProfileHistoryContent(
            state = ProfileHistoryState(
                loading = DONE,
                items = emptyList<HomeActivityItem>().toImmutableList(),
            ),
        )
    }
}
