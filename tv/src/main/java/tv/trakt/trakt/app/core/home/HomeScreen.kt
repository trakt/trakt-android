package tv.trakt.trakt.app.core.home

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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.AUTHENTICATED
import tv.trakt.trakt.app.core.home.HomeState.AuthenticationState.UNAUTHENTICATED
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.HomeAvailableNowView
import tv.trakt.trakt.app.core.home.sections.movies.comingsoon.HomeComingSoonView
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.HomeUpcomingView
import tv.trakt.trakt.app.core.home.sections.shows.upnext.HomeUpNextView
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId

private val sections = listOf(
    "upNextEpisodes",
    "upcomingEpisodes",
    "availableNow",
    "comingSoon",
)

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
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
        )
    }
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    modifier: Modifier = Modifier,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
) {
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
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
            imageUrl = focusedImageUrl ?: state.backgroundUrl,
            saturation = 0F,
            crossfade = true,
        )

        val sectionPadding = PaddingValues(
            start = TraktTheme.spacing.mainContentStartSpace,
            end = TraktTheme.spacing.mainContentEndSpace,
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
                HomeUpNextView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onLoaded = {
                        focusRequesters
                            .getValue("upNextEpisodes")
                            .requestFocus()
                    },
                    onFocused = { show ->
                        focusedSection = "upNextEpisodes"
                        focusedImageUrl = show.images?.getFanartUrl(Images.Size.FULL)
                    },
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("upNextEpisodes")),
                )
            }

            item {
                HomeUpcomingView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onFocused = { show ->
                        focusedSection = "upcomingEpisodes"
                        focusedImageUrl = show.images?.getFanartUrl(Images.Size.FULL)
                    },
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("upcomingEpisodes")),
                )
            }

            item {
                HomeAvailableNowView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToMovie = onNavigateToMovie,
                    onFocused = { movie ->
                        focusedSection = "availableNow"
                        focusedImageUrl = movie.images?.getFanartUrl(Images.Size.FULL)
                    },
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("availableNow")),
                )
            }

            item {
                HomeComingSoonView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onNavigateToMovie = onNavigateToMovie,
                    onFocused = { movie ->
                        focusedSection = "comingSoon"
                        focusedImageUrl = movie.images?.getFanartUrl(Images.Size.FULL)
                    },
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("comingSoon")),
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
            onNavigateToEpisode = { _, _ -> },
        )
    }
}
