package tv.trakt.trakt.tv.core.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.tv.common.model.Images
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.tv.core.details.ui.BackdropImage
import tv.trakt.trakt.tv.core.lists.views.ListsMoviesWatchlistView
import tv.trakt.trakt.tv.core.lists.views.ListsShowsWatchlistView
import tv.trakt.trakt.tv.ui.theme.TraktTheme

private val sections = listOf(
    "initial",
    "shows",
    "movies",
)

@Composable
internal fun ListsScreen(
    viewModel: ListsViewModel,
    onShowClick: (TraktId) -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onShowViewAllClick: () -> Unit = {},
    onMovieViewAllClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateShowsData()
        viewModel.updateMoviesData()
    }

    ListsScreenContent(
        state = state,
        onShowClick = onShowClick,
        onMovieClick = onMovieClick,
        onShowViewAllClick = onShowViewAllClick,
        onMovieViewAllClick = onMovieViewAllClick,
    )
}

@Composable
internal fun ListsScreenContent(
    modifier: Modifier = Modifier,
    state: ListsState,
    onShowClick: (TraktId) -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onShowViewAllClick: () -> Unit = {},
    onMovieViewAllClick: () -> Unit = {},
) {
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(Unit) {
        focusRequesters["initial"]?.requestFocus()
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedImageUrl,
            saturation = 0F,
            crossfade = true,
        )

        LazyColumn(
            verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
            contentPadding = PaddingValues(
                vertical = TraktTheme.spacing.mainContentVerticalSpace + 8.dp,
            ),
            modifier = Modifier
                .focusRestorer()
                .focusGroup(),
        ) {
            item {
                ListsShowsWatchlistView(
                    items = state.watchlistShows,
                    isLoading = state.isLoading,
                    focusRequesters = focusRequesters,
                    onFocused = {
                        focusedSection = "shows"
                        focusedImageUrl = it?.images?.getFanartUrl(Images.Size.FULL)
                    },
                    onClick = {
                        onShowClick(it.ids.trakt)
                    },
                    onViewAllClick = onShowViewAllClick,
                )
            }
            item {
                ListsMoviesWatchlistView(
                    items = state.watchlistMovies,
                    isLoading = state.isLoading,
                    focusRequesters = focusRequesters,
                    onFocused = {
                        focusedSection = "movies"
                        focusedImageUrl = it?.images?.getFanartUrl(Images.Size.FULL)
                    },
                    onClick = {
                        onMovieClick(it.ids.trakt)
                    },
                    onViewAllClick = onMovieViewAllClick,
                )
            }
        }
    }
}

@Composable
internal fun ListsContentLoading(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            HorizontalMediaSkeletonCard()
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ListsScreenContent(
            state = ListsState(
                watchlistMovies = null,
                watchlistShows = null,
                isLoading = true,
                error = null,
            ),
        )
    }
}
