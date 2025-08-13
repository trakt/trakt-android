package tv.trakt.trakt.core.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.home.navigation.navigateToHome
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.lists.navigation.navigateToLists
import tv.trakt.trakt.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.core.movies.navigation.navigateToMovies
import tv.trakt.trakt.core.search.navigation.SearchDestination
import tv.trakt.trakt.core.search.navigation.navigateToSearch
import tv.trakt.trakt.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.core.shows.navigation.navigateToShows

internal fun NavController.navigateToMainDestination(destination: Any) {
    when (destination) {
        SearchDestination -> navigateToSearch()
        HomeDestination -> navigateToHome()
        ShowsDestination -> navigateToShows()
        MoviesDestination -> navigateToMovies()
        ListsDestination -> navigateToLists()
    }
}

internal fun isMainDestination(destination: NavDestination?): Boolean {
    return destination?.let {
        it.hasRoute<ShowsDestination>() ||
            it.hasRoute<MoviesDestination>() ||
            it.hasRoute<SearchDestination>() ||
            it.hasRoute<ListsDestination>()
    } ?: false
}

internal fun isStartDestination(destination: NavDestination?): Boolean {
    return destination?.hasRoute<HomeDestination>() == true
}
