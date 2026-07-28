package tv.trakt.trakt.app.core.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.AUTHENTICATED
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.UNAUTHENTICATED
import tv.trakt.trakt.app.core.home.sections.history.HomeHistoryView
import tv.trakt.trakt.app.core.home.sections.recommended.HomeRecommendedView
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.HomeUpcomingView
import tv.trakt.trakt.app.core.home.sections.shows.upnext.HomeUpNextView
import tv.trakt.trakt.app.core.home.sections.social.HomeSocialView
import tv.trakt.trakt.app.core.home.sections.startwatching.HomeWatchlistView
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.TraktId
import kotlin.time.Duration.Companion.milliseconds

private val sections = listOf(
    "upNext",
    "upcomingSchedule",
    "recommended",
    "watchlist",
    "socialActivity",
    "history",
)

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToUpNext: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToRecommended: () -> Unit,
    onNavigateToSocialActivity: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.authentication) {
        if (state.authentication == UNAUTHENTICATED) {
            onNavigateToAuth()
        }
    }

    if (state.authentication == AUTHENTICATED) {
        HomeScreenContent(
            state = state,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToShow = onNavigateToShow,
            onNavigateToUpNext = onNavigateToUpNext,
            onNavigateToWatchlist = onNavigateToWatchlist,
            onNavigateToRecommended = onNavigateToRecommended,
            onNavigateToSocialActivity = onNavigateToSocialActivity,
            onNavigateToHistory = onNavigateToHistory,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun HomeScreenContent(
    state: HomeState,
    modifier: Modifier = Modifier,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToUpNext: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToRecommended: () -> Unit,
    onNavigateToSocialActivity: () -> Unit,
    onNavigateToHistory: () -> Unit,
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
            focusedSection = "upNext"
            focusRequesters["upNext"]?.requestSafeFocus()
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
            imageUrl = focusedImageUrl ?: state.backgroundUrl,
            saturation = 0F,
            crossfade = true,
        )

        val sectionPadding = PaddingValues(
            start = TraktTheme.spacing.mainContentStartSpace,
            end = TraktTheme.spacing.mainContentEndSpace,
        )

        LazyColumn(
            state = rememberLazyListState(
                cacheWindow = LazyLayoutCacheWindow(
                    aheadFraction = 1F,
                    behindFraction = 1F,
                ),
            ),
            verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
            contentPadding = PaddingValues(
                vertical = TraktTheme.spacing.mainContentVerticalSpace + 8.dp,
            ),
            modifier = Modifier
                .focusRestorer()
                .focusProperties {
                    // Prevent focus escaping the list vertically (e.g. to the side
                    // menu) when a fast D-pad scroll targets a not-yet-focusable item.
                    exit = { direction ->
                        when (direction) {
                            FocusDirection.Down, FocusDirection.Up -> FocusRequester.Cancel
                            else -> FocusRequester.Default
                        }
                    }
                }
                .focusGroup(),
        ) {
            item {
                HomeUpNextView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onNavigateToMovie = onNavigateToMovie,
                    onNavigateToViewAll = onNavigateToUpNext,
                    onLoaded = {
                        if (!focusedInitial) {
                            focusedInitial = true
                            focusRequesters["upNext"]?.requestSafeFocus()
                        } else {
                            refocusOnLoad("upNext")
                        }
                    },
                    onFocused = { images ->
                        focusedSection = "upNext"
                        focusedImageUrl = images?.getFanartUrl(Size.FULL)
                    },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("upNext")),
                )
            }

            item {
                HomeWatchlistView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToMovie = onNavigateToMovie,
                    onNavigateToShow = onNavigateToShow,
                    onNavigateToViewAll = onNavigateToWatchlist,
                    onFocused = { item ->
                        focusedSection = "watchlist"
                        focusedImageUrl = item?.fullFanartImage
                    },
                    onLoaded = { refocusOnLoad("watchlist") },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("watchlist")),
                )
            }

            item {
                HomeUpcomingView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onNavigateToMovie = onNavigateToMovie,
                    onFocused = { show ->
                        focusedSection = "upcomingSchedule"
                        focusedImageUrl = show?.images?.getFanartUrl(Size.FULL)
                    },
                    onLoaded = { refocusOnLoad("upcomingSchedule") },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("upcomingSchedule")),
                )
            }

            item {
                HomeRecommendedView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToShow = onNavigateToShow,
                    onNavigateToMovie = onNavigateToMovie,
                    onNavigateToViewAll = onNavigateToRecommended,
                    onFocused = { item ->
                        focusedSection = "recommended"
                        focusedImageUrl = item?.fullFanartImage
                    },
                    onLoaded = { refocusOnLoad("recommended") },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("recommended")),
                )
            }

            item {
                HomeHistoryView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToMovie = onNavigateToMovie,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onNavigateToViewAll = onNavigateToHistory,
                    onFocused = { item ->
                        focusedSection = "history"
                        focusedImageUrl = item?.backdropImageUrl
                    },
                    onLoaded = { refocusOnLoad("history") },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("history")),
                )
            }

            item {
                HomeSocialView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToMovie = onNavigateToMovie,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onNavigateToViewAll = onNavigateToSocialActivity,
                    onFocused = { item ->
                        focusedSection = "socialActivity"
                        focusedImageUrl = item?.images?.getFanartUrl(Size.FULL)
                    },
                    onLoaded = { refocusOnLoad("socialActivity") },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("socialActivity")),
                )
            }
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun MainScreenPreview() {
    TraktTheme {
        HomeScreenContent(
            state = HomeState(),
            onNavigateToMovie = {},
            onNavigateToShow = {},
            onNavigateToEpisode = { _, _ -> },
            onNavigateToUpNext = {},
            onNavigateToWatchlist = {},
            onNavigateToRecommended = {},
            onNavigateToSocialActivity = {},
            onNavigateToHistory = {},
        )
    }
}
