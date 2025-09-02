package tv.trakt.trakt.core.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onProfileClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localBottomBarVisibility = LocalBottomBarVisibility.current
    LaunchedEffect(Unit) {
        localBottomBarVisibility.value = true
    }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it.ids.trakt)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it.ids.trakt)
        }
    }

    SearchScreenContent(
        state = state,
        onSearchQuery = viewModel::searchQuery,
        onShowClick = onShowClick,
        onMovieClick = onMovieClick,
        onProfileClick = onProfileClick,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchScreenContent(
    state: SearchState,
    modifier: Modifier = Modifier,
    onSearchQuery: (String) -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val lazyListState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val isScrolledToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
            modifier = Modifier.graphicsLayer {
                if (lazyListState.firstVisibleItemIndex == 0) {
                    translationY = (-0.75F * lazyListState.firstVisibleItemScrollOffset)
                } else {
                    alpha = 0F
                }
            },
        )

        SearchScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
private fun SearchScreenHeader(
    state: SearchState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onProfileClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == DONE
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showVip = headerState.startScrolled,
        showProfile = userState.first && userState.second,
        showJoinTrakt = userState.first && !userState.second,
        userVip = state.user.user?.isAnyVip ?: false,
        userAvatar = state.user.user?.images?.avatar?.full,
        onJoinClick = onProfileClick,
        onProfileClick = onProfileClick,
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
        SearchScreenContent(
            state = SearchState(),
        )
    }
}
