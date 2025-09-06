package tv.trakt.trakt.core.main

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.home.navigation.homeScreen
import tv.trakt.trakt.core.lists.navigation.listsScreen
import tv.trakt.trakt.core.lists.navigation.navigateToLists
import tv.trakt.trakt.core.main.navigation.isMainDestination
import tv.trakt.trakt.core.main.navigation.isStartDestination
import tv.trakt.trakt.core.main.navigation.navigateToMainDestination
import tv.trakt.trakt.core.main.ui.menubar.TraktMenuBar
import tv.trakt.trakt.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.core.movies.navigation.moviesScreen
import tv.trakt.trakt.core.movies.navigation.navigateToMovies
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.profile.navigation.profileScreen
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.navigateToSearch
import tv.trakt.trakt.core.search.navigation.searchScreen
import tv.trakt.trakt.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.core.shows.navigation.navigateToShows
import tv.trakt.trakt.core.shows.navigation.showsScreen
import tv.trakt.trakt.ui.snackbar.MainSnackbarHost
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MainScreen(
    modifier: Modifier = Modifier,
    intent: Intent? = null,
) {
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
        MainNavHost(
            navController = navController,
            searchInput = searchState.searchInput,
            onSearchLoading = searchState.onSearchLoading,
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
                    onSearchInput = searchState.onSearchInput,
                )
            }
        }

        MainSnackbarHost(
            snackbarHostState = localSnackbar,
        )
    }

    BackHandler {
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
    searchInput: SearchInput,
    onSearchLoading: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        startDestination = HomeDestination,
        navController = navController,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
    ) {
        with(navController) {
            homeScreen(
                onNavigateToProfile = { navigateToProfile() },
                onNavigateToShows = { navigateToMainDestination(ShowsDestination) },
                onNavigateToMovies = { navigateToMainDestination(MoviesDestination) },
            )
            showsScreen(
                onNavigateToProfile = { navigateToProfile() },
                onNavigateToShow = {},
            )
            moviesScreen(
                onNavigateToProfile = { navigateToProfile() },
                onNavigateToMovie = {},
            )
            listsScreen(onNavigateToList = {})
            searchScreen(
                searchInput = searchInput,
                onSearchLoading = onSearchLoading,
                onNavigateToShow = { },
                onNavigateToMovie = { },
                onNavigateToProfile = { navigateToProfile() },
            )
            profileScreen(
                onNavigateBack = { popBackStack() },
            )
        }
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

@Preview
@Composable
private fun MainScreenPreview() {
    TraktTheme {
        MainScreen()
    }
}
