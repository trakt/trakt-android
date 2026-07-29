package tv.trakt.trakt.app.core.lists

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
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.lists.views.ListsLikedView
import tv.trakt.trakt.app.core.lists.views.ListsMoviesWatchlistView
import tv.trakt.trakt.app.core.lists.views.ListsPersonalView
import tv.trakt.trakt.app.core.lists.views.ListsShowsWatchlistView
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import kotlin.time.Duration.Companion.milliseconds

private val sections = listOf(
    "shows",
    "movies",
    "personal",
    "liked",
)

@Composable
internal fun ListsScreen(
    viewModel: ListsViewModel,
    onListClick: (CustomList) -> Unit = {},
    onLikedListClick: (CustomList) -> Unit = {},
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

    LifecycleEventEffect(ON_START) {
        viewModel.updateLikedListsData()
    }

    ListsScreenContent(
        state = state,
        onListClick = onListClick,
        onLikedListClick = onLikedListClick,
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
    onListClick: (CustomList) -> Unit = {},
    onLikedListClick: (CustomList) -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onShowViewAllClick: () -> Unit = {},
    onMovieViewAllClick: () -> Unit = {},
) {
    var focusedInitial by rememberSaveable { mutableStateOf(false) }
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    // When a section swaps its loading skeletons for real content, the focused
    // skeleton is disposed and focus would be lost. Re-request focus on the
    // section the user was on.
    val refocusOnLoad: (String) -> Unit = { section ->
        if (focusedSection == section) {
            focusRequesters[section]?.requestSafeFocus()
        }
    }

    LaunchedEffect(Unit) {
        if (focusedSection == null) {
            focusedSection = "shows"
            focusRequesters["shows"]?.requestSafeFocus()
        } else {
            delay(500.milliseconds)
            focusRequesters[focusedSection]?.requestSafeFocus()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestSafeFocus()
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
                    isLoading = state.loadingLists.loadingWatchlist,
                    focusRequesters = focusRequesters,
                    onLoaded = {
                        if (!focusedInitial) {
                            focusedInitial = true
                            focusRequesters["shows"]?.requestSafeFocus()
                        } else {
                            refocusOnLoad("shows")
                        }
                    },
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
                    isLoading = state.loadingLists.loadingWatchlist,
                    focusRequesters = focusRequesters,
                    onLoaded = { refocusOnLoad("movies") },
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

            item {
                ListsPersonalView(
                    items = state.personalLists,
                    isLoading = state.loadingLists.loadingPersonal ||
                        state.loadingLists.loadingWatchlist,
                    focusRequesters = focusRequesters,
                    onLoaded = { refocusOnLoad("personal") },
                    onFocused = {
                        focusedSection = "personal"
                        focusedImageUrl = null
                    },
                    onClick = {
                        onListClick(it)
                    },
                )
            }

            item {
                ListsLikedView(
                    items = state.likedLists,
                    isLoading = state.loadingLists.loadingLiked ||
                        state.loadingLists.loadingWatchlist,
                    focusRequesters = focusRequesters,
                    onLoaded = { refocusOnLoad("liked") },
                    onFocused = {
                        focusedSection = "liked"
                        focusedImageUrl = null
                    },
                    onClick = {
                        onLikedListClick(it)
                    },
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
            VerticalMediaSkeletonCard()
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
                error = null,
            ),
        )
    }
}
