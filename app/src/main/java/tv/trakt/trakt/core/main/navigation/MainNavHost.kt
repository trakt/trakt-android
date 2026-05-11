package tv.trakt.trakt.core.main.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.search.model.SearchInput

@Composable
internal fun MainNavHost(
    navController: NavHostController,
    customThemeEnabled: Boolean,
    userLoading: Boolean,
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
        homeScreens(
            controller = navController,
            userLoading = userLoading,
        )
        calendarScreens(navController)
        discoverScreens(
            controller = navController,
            customThemeEnabled = customThemeEnabled,
        )
        showsScreens(navController)
        moviesScreens(navController)
        episodesScreens(navController)
        listsScreens(navController)
        profileScreens(navController)
        commentsScreens(navController)
        allShowSeasonsScreens(navController)
        triviaScreens(navController)
        peopleScreens(navController)
        searchScreens(
            controller = navController,
            searchInput = searchInput,
            onSearchLoading = onSearchLoading,
        )
        settingsScreens(navController)
        billingScreens(navController)
        youTubePlayerScreens(navController)
    }
}
