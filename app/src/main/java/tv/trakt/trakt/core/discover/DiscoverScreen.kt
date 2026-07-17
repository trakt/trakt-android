@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.discover

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.sections.anticipated.DiscoverAnticipatedView
import tv.trakt.trakt.core.discover.sections.popular.DiscoverPopularView
import tv.trakt.trakt.core.discover.sections.releases.DiscoverReleasesView
import tv.trakt.trakt.core.discover.sections.trending.DiscoverTrendingView
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToAllTrending: () -> Unit,
    onNavigateToAllPopular: () -> Unit,
    onNavigateToAllAnticipated: () -> Unit,
    onNavigateToAllReleases: () -> Unit,
    onNavigateToVip: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var filtersSheet by remember { mutableStateOf(false) }

    DiscoverScreen(
        state = state,
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
        onEpisodeClick = onNavigateToEpisode,
        onMoreTrendingClick = onNavigateToAllTrending,
        onMorePopularClick = onNavigateToAllPopular,
        onMoreAnticipatedClick = onNavigateToAllAnticipated,
        onMoreReleasesClick = onNavigateToAllReleases,
        onVipClick = onNavigateToVip,
        onFiltersClick = {
            filtersSheet = true
        },
    )

    GlobalFiltersSheet(
        active = filtersSheet,
        onDismiss = {
            filtersSheet = false
        },
    )
}

@Composable
private fun DiscoverScreen(
    state: DiscoverState,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit = {},
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onMoreTrendingClick: () -> Unit = {},
    onMorePopularClick: () -> Unit = {},
    onMoreAnticipatedClick: () -> Unit = {},
    onMoreReleasesClick: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onFiltersClick: () -> Unit = {},
) {
    val activity = LocalActivity.current
    val customThemeEnabled = (activity as? MainActivity)?.customThemeConfig?.enabled == true

    val lazyListState = rememberLazyListState()
    val headerState = rememberHeaderState()

    val isScrolledToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            headerState.resetScrolled()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        ScrollableBackdropImage(
            scrollState = lazyListState,
        )

        val listPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding()
                .plus(TraktTheme.spacing.mainPageTopSpace),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight)
                .plus(TraktTheme.spacing.mainPageBottomSpace),
        )

        val sectionPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        )

        LazyColumn(
            state = lazyListState,
            overscrollEffect = null,
            verticalArrangement = spacedBy(TraktTheme.spacing.mainSectionVerticalSpace),
            contentPadding = listPadding,
        ) {
            item {
                DiscoverTrendingView(
                    viewModel = koinViewModel {
                        parametersOf(customThemeEnabled)
                    },
                    collection = state.collection,
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMovieClick = onMovieClick,
                    onMoreClick = onMoreTrendingClick,
                )
            }

            item {
                DiscoverReleasesView(
                    viewModel = koinViewModel {
                        parametersOf(customThemeEnabled)
                    },
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMovieClick = onMovieClick,
                    onEpisodeClick = onEpisodeClick,
                    onMoreClick = onMoreReleasesClick,
                )
            }

            item {
                DiscoverAnticipatedView(
                    viewModel = koinViewModel {
                        parametersOf(customThemeEnabled)
                    },
                    collection = state.collection,
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMovieClick = onMovieClick,
                    onMoreClick = onMoreAnticipatedClick,
                )
            }

            item {
                DiscoverPopularView(
                    viewModel = koinViewModel {
                        parametersOf(customThemeEnabled)
                    },
                    collection = state.collection,
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMovieClick = onMovieClick,
                    onMoreClick = onMorePopularClick,
                )
            }
        }

        ScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
            onVipClick = onVipClick,
            onFiltersClick = onFiltersClick,
        )
    }
}

@Composable
private fun ScreenHeader(
    state: DiscoverState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onVipClick: () -> Unit,
    onFiltersClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == Done
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showLogin = userState.first && !userState.second,
        showVip = userState.second && state.user.user?.isVip == false,
        showFilters = true,
        onVipClick = onVipClick,
        onFilterClick = onFiltersClick,
        modifier = Modifier.offset {
            IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
        },
    )
}

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DiscoverScreen(
            state = DiscoverState(),
            onShowClick = {},
        )
    }
}
