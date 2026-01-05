package tv.trakt.trakt.core.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.home.sections.activity.features.history.HomeHistoryView
import tv.trakt.trakt.core.home.sections.activity.features.social.HomeSocialView
import tv.trakt.trakt.core.home.sections.upcoming.HomeUpcomingView
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextView
import tv.trakt.trakt.core.home.sections.watchlist.HomeWatchlistView
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.components.vip.VipBanner
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
    userLoading: Boolean,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToAllUpNext: () -> Unit,
    onNavigateToAllWatchlist: () -> Unit,
    onNavigateToAllPersonal: () -> Unit,
    onNavigateToAllSocial: () -> Unit,
    onNavigateToVip: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state,
        userLoading = userLoading,
        onShowClick = onNavigateToShow,
        onShowsClick = {
            when {
                userLoading -> return@HomeScreenContent
                state.user.user != null -> onNavigateToDiscover()
                else -> uriHandler.openUri(ConfigAuth.authCodeUrl)
            }
        },
        onMoviesClick = {
            when {
                userLoading -> return@HomeScreenContent
                state.user.user != null -> onNavigateToDiscover()
                else -> uriHandler.openUri(ConfigAuth.authCodeUrl)
            }
        },
        onMovieClick = onNavigateToMovie,
        onEpisodeClick = onNavigateToEpisode,
        onMoreUpNextClick = onNavigateToAllUpNext,
        onMoreWatchlistClick = onNavigateToAllWatchlist,
        onMorePersonalClick = onNavigateToAllPersonal,
        onMoreSocialClick = onNavigateToAllSocial,
        onVipClick = onNavigateToVip,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenContent(
    state: HomeState,
    userLoading: Boolean,
    modifier: Modifier = Modifier,
    onMoreUpNextClick: () -> Unit = {},
    onMoreWatchlistClick: () -> Unit = {},
    onMorePersonalClick: () -> Unit = {},
    onMoreSocialClick: () -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onShowsClick: () -> Unit = {},
    onMoviesClick: () -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onVipClick: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val lazyListState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
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

        LazyColumn(
            state = lazyListState,
            overscrollEffect = null,
            verticalArrangement = spacedBy(TraktTheme.spacing.mainSectionVerticalSpace),
            contentPadding = listPadding,
        ) {
            if (state.mode == null || state.mode.isMediaOrShows) {
                item {
                    HomeUpNextView(
                        headerPadding = sectionPadding,
                        contentPadding = sectionPadding,
                        onShowClick = onShowClick,
                        onShowsClick = onShowsClick,
                        onEpisodeClick = onEpisodeClick,
                        onMoreClick = onMoreUpNextClick,
                    )
                }
            }

            item {
                HomeWatchlistView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onShowsClick = onShowsClick,
                    onMovieClick = onMovieClick,
                    onMoviesClick = onMoviesClick,
                    onMoreClick = onMoreWatchlistClick,
                )
            }

            if (state.user.user != null && !state.user.user.isVip) {
                item {
                    VipBanner(
                        onClick = onVipClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(sectionPadding),
                    )
                }
            }

            item {
                HomeUpcomingView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onShowsClick = onShowsClick,
                    onMoviesClick = onMoviesClick,
                    onEpisodeClick = onEpisodeClick,
                    onMovieClick = onMovieClick,
                )
            }

            if (state.user.user != null) {
                item {
                    HomeHistoryView(
                        headerPadding = sectionPadding,
                        contentPadding = sectionPadding,
                        onShowClick = onShowClick,
                        onEpisodeClick = onEpisodeClick,
                        onMovieClick = onMovieClick,
                        onMoreClick = onMorePersonalClick,
                    )
                }
            }

            item {
                HomeSocialView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onMoreClick = onMoreSocialClick,
                    onShowClick = onShowClick,
                    onEpisodeClick = onEpisodeClick,
                    onMovieClick = onMovieClick,
                )
            }
        }

        HomeScreenHeader(
            state = state,
            headerState = headerState,
            userLoading = userLoading,
            isScrolledToTop = isScrolledToTop,
            onVipClick = onVipClick,
        )
    }
}

@Composable
private fun HomeScreenHeader(
    state: HomeState,
    headerState: ScreenHeaderState,
    userLoading: Boolean,
    isScrolledToTop: Boolean,
    onVipClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == DONE
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showLogin = userState.first && !userState.second,
        showVip = userState.second && state.user.user?.isVip == false,
        userLoading = userLoading,
        onVipClick = onVipClick,
        modifier = Modifier.offset {
            IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
        },
    )
}

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeScreenContent(
            state = HomeState(),
            userLoading = false,
        )
    }
}
