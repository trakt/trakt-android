package tv.trakt.trakt.tv.core.main.navigation

import androidx.navigation.NavController
import tv.trakt.trakt.tv.core.auth.navigation.navigateToAuth
import tv.trakt.trakt.tv.core.home.navigation.HomeDestination
import tv.trakt.trakt.tv.core.home.navigation.navigateToHome
import tv.trakt.trakt.tv.core.lists.navigation.ListsDestination
import tv.trakt.trakt.tv.core.lists.navigation.navigateToLists
import tv.trakt.trakt.tv.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.tv.core.movies.navigation.navigateToMovies
import tv.trakt.trakt.tv.core.search.navigation.SearchDestination
import tv.trakt.trakt.tv.core.search.navigation.navigateToSearch
import tv.trakt.trakt.tv.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.tv.core.shows.navigation.navigateToShows

internal fun NavController.navigateToMainDestination(
    destination: Any,
    isSignedIn: Boolean,
) {
    when (destination) {
        SearchDestination -> navigateToSearch()
        HomeDestination -> navigateToHome()
        ShowsDestination -> navigateToShows()
        MoviesDestination -> navigateToMovies()
        ListsDestination -> when {
            isSignedIn -> navigateToLists()
            else -> navigateToAuth()
        }
    }
}
