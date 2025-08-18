package tv.trakt.trakt.app.core.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.DrawerValue.Closed
import androidx.tv.material3.DrawerValue.Open
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.rememberDrawerState
import tv.trakt.trakt.app.LocalDrawerVisibility
import tv.trakt.trakt.app.LocalSnackbarState
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.common.model.SeasonEpisode
import tv.trakt.trakt.app.core.auth.navigation.authScreen
import tv.trakt.trakt.app.core.auth.navigation.navigateToAuth
import tv.trakt.trakt.app.core.details.episode.navigation.episodeDetailsScreen
import tv.trakt.trakt.app.core.details.episode.navigation.navigateToEpisode
import tv.trakt.trakt.app.core.details.lists.details.movies.navigation.customListMovies
import tv.trakt.trakt.app.core.details.lists.details.movies.navigation.navigateToCustomListMovies
import tv.trakt.trakt.app.core.details.lists.details.shows.navigation.customListShows
import tv.trakt.trakt.app.core.details.lists.details.shows.navigation.navigateToCustomListShows
import tv.trakt.trakt.app.core.details.movie.navigation.movieDetailsScreen
import tv.trakt.trakt.app.core.details.movie.navigation.navigateToMovie
import tv.trakt.trakt.app.core.details.show.navigation.navigateToShow
import tv.trakt.trakt.app.core.details.show.navigation.showDetailsScreen
import tv.trakt.trakt.app.core.home.navigation.HomeDestination
import tv.trakt.trakt.app.core.home.navigation.homeScreen
import tv.trakt.trakt.app.core.home.navigation.navigateToHome
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.navigation.homeAvailableNowScreen
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.navigation.navigateToHomeAvailableNow
import tv.trakt.trakt.app.core.home.sections.movies.comingsoon.navigation.homeComingSoonScreen
import tv.trakt.trakt.app.core.home.sections.movies.comingsoon.navigation.navigateToHomeComingSoon
import tv.trakt.trakt.app.core.home.sections.shows.upnext.navigation.homeUpNextScreen
import tv.trakt.trakt.app.core.home.sections.shows.upnext.navigation.navigateToHomeUpNext
import tv.trakt.trakt.app.core.lists.details.movies.navigation.navigateToWatchlistMovies
import tv.trakt.trakt.app.core.lists.details.movies.navigation.watchlistMovies
import tv.trakt.trakt.app.core.lists.details.shows.navigation.navigateToWatchlistShows
import tv.trakt.trakt.app.core.lists.details.shows.navigation.watchlistShows
import tv.trakt.trakt.app.core.lists.navigation.listsScreen
import tv.trakt.trakt.app.core.main.navigation.navigateToMainDestination
import tv.trakt.trakt.app.core.main.ui.drawer.NavigationDrawerContent
import tv.trakt.trakt.app.core.main.ui.snackbar.MainSnackbarHost
import tv.trakt.trakt.app.core.movies.features.anticipated.navigation.moviesAnticipatedScreen
import tv.trakt.trakt.app.core.movies.features.anticipated.navigation.navigateToMoviesAnticipated
import tv.trakt.trakt.app.core.movies.features.popular.navigation.moviesPopularScreen
import tv.trakt.trakt.app.core.movies.features.popular.navigation.navigateToMoviesPopular
import tv.trakt.trakt.app.core.movies.features.recommended.navigation.moviesRecommendedScreen
import tv.trakt.trakt.app.core.movies.features.recommended.navigation.navigateToMoviesRecommended
import tv.trakt.trakt.app.core.movies.features.trending.navigation.moviesTrendingScreen
import tv.trakt.trakt.app.core.movies.features.trending.navigation.navigateToMoviesTrending
import tv.trakt.trakt.app.core.movies.navigation.moviesScreen
import tv.trakt.trakt.app.core.people.navigation.navigateToPerson
import tv.trakt.trakt.app.core.people.navigation.personDetailsScreen
import tv.trakt.trakt.app.core.player.navigateToPlayer
import tv.trakt.trakt.app.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.app.core.profile.navigation.profileScreen
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.viewall.navigation.navigateToProfileFavoriteMoviesViewAll
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.viewall.navigation.profileFavoriteMoviesViewAllScreen
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.viewall.navigation.navigateToProfileFavoriteShowsViewAll
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.viewall.navigation.profileFavoriteShowsViewAllScreen
import tv.trakt.trakt.app.core.profile.sections.history.viewall.navigation.navigateToProfileHistoryViewAll
import tv.trakt.trakt.app.core.profile.sections.history.viewall.navigation.profileHistoryViewAllScreen
import tv.trakt.trakt.app.core.search.navigation.searchScreen
import tv.trakt.trakt.app.core.shows.features.anticipated.navigation.navigateToShowsAnticipated
import tv.trakt.trakt.app.core.shows.features.anticipated.navigation.showsAnticipatedScreen
import tv.trakt.trakt.app.core.shows.features.popular.navigation.navigateToShowsPopular
import tv.trakt.trakt.app.core.shows.features.popular.navigation.showsPopularScreen
import tv.trakt.trakt.app.core.shows.features.recommended.navigation.navigateToShowsRecommended
import tv.trakt.trakt.app.core.shows.features.recommended.navigation.showsRecommendedScreen
import tv.trakt.trakt.app.core.shows.features.trending.navigation.navigateToShowsTrending
import tv.trakt.trakt.app.core.shows.features.trending.navigation.showsTrendingScreen
import tv.trakt.trakt.app.core.shows.navigation.showsScreen
import tv.trakt.trakt.app.core.splash.SplashScreen
import tv.trakt.trakt.app.core.streamings.navigation.allStreamingsScreen
import tv.trakt.trakt.app.core.streamings.navigation.navigateToEpisodeStreamings
import tv.trakt.trakt.app.core.streamings.navigation.navigateToMovieStreamings
import tv.trakt.trakt.app.core.streamings.navigation.navigateToShowStreamings
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localSnackbar = LocalSnackbarState.current
    val localDrawerVisibility = LocalDrawerVisibility.current

    val drawerState = rememberDrawerState(Closed)
    val hostFocusRequester = remember { FocusRequester() }
    val backHandlerEnabled by remember { mutableStateOf(true) }

    val navController = rememberNavController()
    val currentDestination = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut == true) {
            navController.navigateToAuth()
            localSnackbar.showSnackbar(localContext.getString(R.string.info_signed_out))
        }
    }

    ModalNavigationDrawer(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .alpha(if (state.splash == null || state.splash == true) 0f else 1f),
        scrimBrush = horizontalGradient(listOf(Color.Black.copy(alpha = 0.9F), Color.Transparent)),
        drawerState = drawerState,
        drawerContent = { drawerValue ->
            Box(
                modifier = Modifier
                    .requiredWidth(TraktTheme.size.navigationDrawerSize)
                    .alpha(if (localDrawerVisibility.value) 1f else 0f),
            ) {
                NavigationDrawerContent(
                    drawerValue = drawerValue,
                    currentDestination = currentDestination.value?.destination,
                    profile = state.profile,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            top = 24.dp,
                            bottom = 24.dp,
                        )
                        .onPreviewKeyEvent {
                            when {
                                KeyEventType.KeyUp == it.type && Key.DirectionRight == it.key -> {
                                    drawerState.setValue(Closed)
                                    hostFocusRequester.requestFocus()
                                    true
                                }
                                else -> false
                            }
                        },
                    onProfileSelected = {
                        drawerState.setValue(Closed)
                        when (state.profile) {
                            null -> navController.navigateToAuth()
                            else -> navController.navigateToProfile()
                        }
                    },
                    onSelected = {
                        navController.navigateToMainDestination(
                            destination = it.destination,
                            isSignedIn = state.profile != null,
                        )
                    },
                    onReselected = {
                        drawerState.setValue(Closed)
                        hostFocusRequester.requestFocus()
                    },
                )
            }
        },
    ) {
        MainNavHost(
            navController = navController,
            hostFocusRequester = hostFocusRequester,
            onAuthorized = {
                drawerState.setValue(Closed)
                navController.navigateToHome()
            },
        )

        MainSnackbarHost(
            snackbarHostState = localSnackbar,
        )
    }

    if (state.splash == true) {
        SplashScreen(onDismiss = {
            viewModel.dismissSplash()
            drawerState.setValue(Closed)
        })
    }

    BackHandler(backHandlerEnabled) {
        when {
            drawerState.currentValue == Open -> {
                (localContext as? Activity)?.finish()
            }

            navController.previousBackStackEntry == null -> {
                drawerState.setValue(Open)
            }

            else -> navController.popBackStack()
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    hostFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onAuthorized: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier.focusRequester(hostFocusRequester),
    ) {
        with(navController) {
            authScreen(
                onAuthorized = onAuthorized,
            )
            searchScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToMovie = { navigateToMovie(it) },
            )
            homeScreen(
                onNavigateToAuth = { navigateToAuth() },
                onNavigateToMovie = { navigateToMovie(it) },
                onNavigateToEpisode = { showId, episodeId ->
                    navigateToEpisode(showId, episodeId)
                },
                onNavigateToUpNext = { navigateToHomeUpNext() },
                onNavigateToAvailableNow = { navigateToHomeAvailableNow() },
                onNavigateToComingSoon = { navigateToHomeComingSoon() },
            )
            homeUpNextScreen(
                onNavigateToEpisode = { showId, episode ->
                    navigateToEpisode(showId, episode)
                },
            )
            homeAvailableNowScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            homeComingSoonScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            profileScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToMovie = { navigateToMovie(it) },
                onNavigateToEpisode = { showId, episodeId ->
                    navigateToEpisode(showId, episodeId)
                },
                onNavigateToHistoryViewAll = { navigateToProfileHistoryViewAll() },
                onNavigateToFavShowsViewAll = { navigateToProfileFavoriteShowsViewAll() },
                onNavigateToFavMoviesViewAll = { navigateToProfileFavoriteMoviesViewAll() },
            )
            profileHistoryViewAllScreen(
                onNavigateToMovie = { navigateToMovie(it) },
                onNavigateToEpisode = { showId, episode ->
                    navigateToEpisode(showId, episode)
                },
            )
            profileFavoriteShowsViewAllScreen(
                onNavigateToShow = { navigateToShow(it) },
            )
            profileFavoriteMoviesViewAllScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            showsScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToTrending = { navigateToShowsTrending() },
                onNavigateToPopular = { navigateToShowsPopular() },
                onNavigateToAnticipated = { navigateToShowsAnticipated() },
                onNavigateToRecommended = { navigateToShowsRecommended() },
            )
            showsTrendingScreen(
                onNavigateToShow = { navigateToShow(it) },
            )
            showsPopularScreen(
                onNavigateToShow = { navigateToShow(it) },
            )
            showsAnticipatedScreen(
                onNavigateToShow = { navigateToShow(it) },
            )
            showsRecommendedScreen(
                onNavigateToShow = { navigateToShow(it) },
            )
            moviesScreen(
                onNavigateToMovie = { navigateToMovie(it) },
                onNavigateToTrending = { navigateToMoviesTrending() },
                onNavigateToPopular = { navigateToMoviesPopular() },
                onNavigateToAnticipated = { navigateToMoviesAnticipated() },
                onNavigateToRecommended = { navigateToMoviesRecommended() },
            )
            moviesTrendingScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            moviesPopularScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            moviesAnticipatedScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            moviesRecommendedScreen(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            listsScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToMovie = { navigateToMovie(it) },
                onNavigateToWatchlistShow = { navigateToWatchlistShows() },
                onNavigateToWatchlistMovie = { navigateToWatchlistMovies() },
            )
            watchlistMovies(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            watchlistShows(
                onNavigateToShow = { navigateToShow(it) },
            )
            showDetailsScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToEpisode = { showId, episodeId ->
                    navigateToEpisode(showId, episodeId)
                },
                onNavigateToPerson = { navigateToPerson(it) },
                onNavigateToList = { navigateToCustomListShows(it) },
                onNavigateToVideo = { navigateToPlayer(it) },
                onNavigateToStreamings = { navigateToShowStreamings(mediaId = it) },
            )
            movieDetailsScreen(
                onNavigateToMovie = { navigateToMovie(it) },
                onNavigateToPerson = { navigateToPerson(it) },
                onNavigateToList = { navigateToCustomListMovies(it) },
                onNavigateToVideo = { navigateToPlayer(it) },
                onNavigateToStreamings = { navigateToMovieStreamings(mediaId = it) },
            )
            episodeDetailsScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToEpisode = { showId, episode ->
                    navigateToEpisode(showId, episode)
                },
                onNavigateToPerson = { navigateToPerson(it) },
                onNavigateToStreamings = { mediaId, episode ->
                    navigateToEpisodeStreamings(
                        mediaId = mediaId,
                        seasonEpisode = SeasonEpisode(
                            season = episode.season,
                            episode = episode.number,
                        ),
                    )
                },
            )
            personDetailsScreen(
                onNavigateToShow = { navigateToShow(it) },
                onNavigateToMovie = { navigateToMovie(it) },
            )
            customListShows(
                onNavigateToShow = { navigateToShow(it) },
            )
            customListMovies(
                onNavigateToMovie = { navigateToMovie(it) },
            )
            allStreamingsScreen()
        }
    }
}
