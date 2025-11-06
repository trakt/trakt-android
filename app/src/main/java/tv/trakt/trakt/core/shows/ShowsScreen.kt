package tv.trakt.trakt.core.shows

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.sections.anticipated.ShowsAnticipatedView
import tv.trakt.trakt.core.shows.sections.popular.ShowsPopularView
import tv.trakt.trakt.core.shows.sections.recommended.ShowsRecommendedView
import tv.trakt.trakt.core.shows.sections.trending.ShowsTrendingView
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsScreen(
    viewModel: ShowsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToAllTrending: () -> Unit,
    onNavigateToAllPopular: () -> Unit,
    onNavigateToAllAnticipated: () -> Unit,
    onNavigateToAllRecommended: () -> Unit,
) {
    val localActivity = LocalActivity.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isHalloween = remember {
        (localActivity as? MainActivity)?.halloweenConfig?.enabled == true
    }

    ShowsScreenContent(
        state = state,
        halloween = isHalloween,
        onShowClick = onNavigateToShow,
        onMoreTrendingClick = onNavigateToAllTrending,
        onMorePopularClick = onNavigateToAllPopular,
        onMoreAnticipatedClick = onNavigateToAllAnticipated,
        onMoreRecommendedClick = onNavigateToAllRecommended,
    )
}

@Composable
private fun ShowsScreenContent(
    state: ShowsState,
    halloween: Boolean,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onMoreTrendingClick: () -> Unit = {},
    onMorePopularClick: () -> Unit = {},
    onMoreAnticipatedClick: () -> Unit = {},
    onMoreRecommendedClick: () -> Unit = {},
) {
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
            imageUrl = state.backgroundUrl,
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
                ShowsTrendingView(
                    viewModel = koinViewModel(
                        parameters = { parametersOf(halloween) },
                    ),
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMoreClick = onMoreTrendingClick,
                )
            }

            item {
                ShowsAnticipatedView(
                    viewModel = koinViewModel(
                        parameters = { parametersOf(halloween) },
                    ),
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMoreClick = onMoreAnticipatedClick,
                )
            }

            item {
                ShowsPopularView(
                    viewModel = koinViewModel(
                        parameters = { parametersOf(halloween) },
                    ),
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowClick = onShowClick,
                    onMoreClick = onMorePopularClick,
                )
            }

            if (state.user.isAuthenticated) {
                item {
                    ShowsRecommendedView(
                        viewModel = koinViewModel(
                            parameters = { parametersOf(halloween) },
                        ),
                        headerPadding = sectionPadding,
                        contentPadding = sectionPadding,
                        onShowClick = onShowClick,
                        onMoreClick = onMoreRecommendedClick,
                    )
                }
            }
        }

        ShowsScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
        )
    }
}

@Composable
private fun ShowsScreenHeader(
    state: ShowsState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == DONE
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showVip = headerState.startScrolled,
        showLogin = userState.first && !userState.second,
        userVip = state.user.user?.isAnyVip ?: false,
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
        ShowsScreenContent(
            state = ShowsState(),
            halloween = false,
            onShowClick = {},
        )
    }
}
