package tv.trakt.trakt.core.main

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LaunchedUpdateEffect
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.lists.navigation.navigateToLists
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.navigateToWatchlist
import tv.trakt.trakt.core.main.navigation.commentsScreens
import tv.trakt.trakt.core.main.navigation.episodesScreens
import tv.trakt.trakt.core.main.navigation.homeScreens
import tv.trakt.trakt.core.main.navigation.isMainDestination
import tv.trakt.trakt.core.main.navigation.isStartDestination
import tv.trakt.trakt.core.main.navigation.listsScreens
import tv.trakt.trakt.core.main.navigation.moviesScreens
import tv.trakt.trakt.core.main.navigation.navigateToMainDestination
import tv.trakt.trakt.core.main.navigation.peopleScreens
import tv.trakt.trakt.core.main.navigation.profileScreens
import tv.trakt.trakt.core.main.navigation.searchScreens
import tv.trakt.trakt.core.main.navigation.showsScreens
import tv.trakt.trakt.core.main.ui.menubar.TraktMenuBar
import tv.trakt.trakt.core.movies.navigation.navigateToMovies
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.navigateToSearch
import tv.trakt.trakt.core.shows.navigation.navigateToShows
import tv.trakt.trakt.core.welcome.WelcomeScreen
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.snackbar.MainSnackbarHost
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    intent: Intent? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localSnackbar = LocalSnackbarState.current
    val localBottomBarVisibility = LocalBottomBarVisibility.current

    val navController = rememberNavController()
    val currentDestination = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = null)

    val searchState = rememberSearchState(
        currentDestination = currentDestination.value?.destination,
    )

    LifecycleEventEffect(ON_RESUME) {
        viewModel.loadData()
    }

    LaunchedUpdateEffect(state.user) {
        if (state.loadingUser == DONE && state.user != null) {
            localSnackbar.showSnackbar(
                message = localContext.getString(R.string.text_info_signed_in),
                duration = SnackbarDuration.Short,
            )
        } else if (state.user == null) {
            localSnackbar.showSnackbar(
                message = localContext.getString(R.string.text_info_signed_out),
                duration = SnackbarDuration.Short,
            )
        }
    }

    LaunchedEffect(intent) {
        if (intent != null) {
            handleShortcutIntent(
                intent = intent,
                navController = navController,
                onRequestFocus = searchState.onRequestFocus,
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Crossfade(
            targetState = state.welcome,
            animationSpec = tween(500),
        ) { isWelcome ->
            if (isWelcome) {
                WelcomeScreen(
                    onDismiss = viewModel::dismissWelcome,
                )
            } else {
                MainNavHost(
                    navController = navController,
                    searchInput = searchState.searchInput,
                    userLoading = state.loadingUser.isLoading,
                    onSearchLoading = searchState.onSearchLoading,
                    onHalloweenCheck = {
                        viewModel.toggleHalloween(
                            enabled = it,
                            context = localContext,
                        )
                    },
                )

                AnimatedVisibility(
                    visible = localBottomBarVisibility.value,
                    enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier
                        .align(BottomCenter),
                ) {
                    NavigationBar(
                        containerColor = TraktTheme.colors.navigationContainer,
                        contentColor = TraktTheme.colors.accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .dropShadow(
                                shape = RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp,
                                ),
                                shadow = Shadow(
                                    radius = 6.dp,
                                    color = Color.Black,
                                    spread = 2.dp,
                                    alpha = 0.25F,
                                ),
                            )
                            .clip(
                                RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp,
                                ),
                            ),
                    ) {
                        TraktMenuBar(
                            currentDestination = currentDestination.value?.destination,
                            enabled = localBottomBarVisibility.value,
                            searchState = searchState,
                            onSelected = {
                                navController.navigateToMainDestination(it.destination)
                            },
                            onReselected = {
                                currentDestination.value?.destination?.let {
                                    if (it.hasRoute(ListsDestination::class) && state.user != null) {
                                        navController.navigateToWatchlist()
                                    }
                                }
                            },
                            onSearchInput = searchState.onSearchInput,
                        )
                    }
                }

                MainSnackbarHost(
                    snackbarHostState = localSnackbar,
                )
            }
        }
    }

    BackHandler(
        enabled = !state.welcome,
    ) {
        with(currentDestination.value?.destination) {
            if (isStartDestination(this)) {
                (localContext as? Activity)?.finish()
                return@BackHandler
            }
            if (isMainDestination(this)) {
                navController.navigateToMainDestination(HomeDestination)
                return@BackHandler
            }
        }

        navController.popBackStack()
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    userLoading: Boolean,
    searchInput: SearchInput,
    onSearchLoading: (Boolean) -> Unit,
    onHalloweenCheck: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        startDestination = HomeDestination,
        navController = navController,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
    ) {
        homeScreens(
            controller = navController,
            userLoading = userLoading,
            onHalloweenCheck = onHalloweenCheck,
        )
        showsScreens(navController)
        episodesScreens(navController)
        moviesScreens(navController)
        listsScreens(navController)
        profileScreens(navController)
        commentsScreens(navController)
        peopleScreens(navController)
        searchScreens(
            controller = navController,
            searchInput = searchInput,
            onSearchLoading = onSearchLoading,
        )
    }
}

private fun handleShortcutIntent(
    intent: Intent,
    navController: NavController,
    onRequestFocus: () -> Unit = {},
) {
    with(intent.extras ?: return) {
        when {
            containsKey("shortcutSearchExtra") -> {
                intent.removeExtra("shortcutSearchExtra")
                navController.navigateToSearch()
                onRequestFocus()
            }
            containsKey("shortcutShowsExtra") -> {
                intent.removeExtra("shortcutShowsExtra")
                navController.navigateToShows()
            }
            containsKey("shortcutMoviesExtra") -> {
                intent.removeExtra("shortcutMoviesExtra")
                navController.navigateToMovies()
            }
            containsKey("shortcutListsExtra") -> {
                intent.removeExtra("shortcutListsExtra")
                navController.navigateToLists()
            }
        }
    }
}
