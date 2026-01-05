package tv.trakt.trakt.core.home.sections.upcoming

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_3
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.home.sections.upcoming.views.EpisodeUpcomingItemView
import tv.trakt.trakt.core.home.sections.upcoming.views.MovieUpcomingItemView
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeUpcomingView(
    modifier: Modifier = Modifier,
    viewModel: HomeUpcomingViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onShowsClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
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

    HomeUpcomingContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onEmptyClick = {
            when (state.filter) {
                MediaMode.MOVIES -> onMoviesClick()
                else -> onShowsClick()
            }
        },
        onClick = {
            viewModel.navigateToEpisode(it.show, it.episode)
        },
        onShowClick = {
            viewModel.navigateToShow(it.show)
        },
        onMovieClick = {
            viewModel.navigateToMovie(it)
        },
    )
}

@Composable
internal fun HomeUpcomingContent(
    state: HomeUpcomingState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: (HomeUpcomingItem.EpisodeItem) -> Unit = {},
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onEmptyClick: () -> Unit = {},
    onCollapse: (collapsed: Boolean) -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_upcoming_schedule),
            chevron = false,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding),
        )

        if (state.collapsed != true) {
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
                            val imageUrl = remember {
                                Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_3).ifBlank { null }
                            }
                            HomeEmptyView(
                                text = stringResource(R.string.text_cta_upcoming),
                                icon = R.drawable.ic_empty_upcoming,
                                buttonText = stringResource(
                                    when (state.filter) {
                                        MediaMode.MOVIES -> R.string.link_text_discover_movies
                                        else -> R.string.link_text_discover_shows
                                    },
                                ),
                                backgroundImageUrl = imageUrl,
                                backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
                                onClick = onEmptyClick,
                                modifier = Modifier
                                    .padding(contentPadding),
                            )
                        }

                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onClick = onClick,
                                onShowClick = onShowClick,
                                onMovieClick = onMovieClick,
                            )
                        }
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
    listItems: ImmutableList<HomeUpcomingItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onMovieClick: (Movie) -> Unit,
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
            key = { it.id.value },
        ) { item ->
            when (item) {
                is HomeUpcomingItem.MovieItem -> {
                    MovieUpcomingItemView(
                        item = item,
                        onClick = { onMovieClick(item.movie) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
                }

                is HomeUpcomingItem.EpisodeItem -> {
                    EpisodeUpcomingItemView(
                        item = item,
                        onClick = { onClick(item) },
                        onShowClick = { onShowClick(item) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
                }
            }
        }
    }
}
