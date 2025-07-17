package tv.trakt.trakt.core.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.home.navigation.homeScreen
import tv.trakt.trakt.core.lists.navigation.listsScreen
import tv.trakt.trakt.core.main.navigation.navigateToMainDestination
import tv.trakt.trakt.core.main.ui.menubar.TraktNavigationBar
import tv.trakt.trakt.core.movies.navigation.moviesScreen
import tv.trakt.trakt.core.search.navigation.searchScreen
import tv.trakt.trakt.core.shows.navigation.showsScreen
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MainScreen(modifier: Modifier = Modifier) {
    val localContext = LocalContext.current
    val navController = rememberNavController()

    val currentDestination = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = null)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        MainNavHost(
            navController = navController,
        )

        NavigationBar(
            containerColor = TraktTheme.colors.navigationContainer,
            contentColor = TraktTheme.colors.accent,
            modifier = Modifier
                .align(BottomCenter)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                    ),
                ),
        ) {
            TraktNavigationBar(
                currentDestination = currentDestination.value?.destination,
                onSelected = {
                    navController.navigateToMainDestination(it.destination)
                },
            )
        }
    }

    BackHandler {
        when {
            currentDestination.value?.destination?.hasRoute<HomeDestination>() != true -> {
                navController.navigateToMainDestination(HomeDestination)
            }
            else -> (localContext as? Activity)?.finish()
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        startDestination = HomeDestination,
        navController = navController,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        with(navController) {
            homeScreen(onNavigateToHome = {})
            showsScreen(onNavigateToShow = {})
            moviesScreen(onNavigateToMovie = {})
            listsScreen(onNavigateToList = {})
            searchScreen(onNavigateToSearch = {})
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
