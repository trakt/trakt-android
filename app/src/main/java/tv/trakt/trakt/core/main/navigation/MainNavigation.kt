package tv.trakt.trakt.core.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import tv.trakt.trakt.core.discover.navigation.DiscoverDestination
import tv.trakt.trakt.core.discover.navigation.navigateToDiscover
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.home.navigation.navigateToHome
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.lists.navigation.navigateToLists
import tv.trakt.trakt.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.core.movies.navigation.navigateToMovies
import tv.trakt.trakt.core.profile.navigation.ProfileDestination
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.search.navigation.SearchDestination
import tv.trakt.trakt.core.search.navigation.navigateToSearch

private val mainDestinations = setOf(
    DiscoverDestination::class,
    MoviesDestination::class,
    ListsDestination::class,
    SearchDestination::class,
    ProfileDestination::class,
)

internal fun NavController.navigateToMainDestination(destination: Any) {
    when (destination) {
        HomeDestination -> navigateToHome()
        DiscoverDestination -> navigateToDiscover()
        MoviesDestination -> navigateToMovies()
        ListsDestination -> navigateToLists()
        SearchDestination -> navigateToSearch()
        ProfileDestination -> navigateToProfile()
    }
}

internal fun isStartDestination(destination: NavDestination?): Boolean {
    return destination?.hasRoute<HomeDestination>() == true
}

internal fun isMainDestination(destination: NavDestination?): Boolean {
    return destination?.let {
        mainDestinations.any { destination -> it.hasRoute(destination) }
    } ?: false
}

internal fun isNonSearchDestination(destination: NavDestination?): Boolean {
    return isStartDestination(destination) ||
        destination?.hasRoute(DiscoverDestination::class) == true ||
        destination?.hasRoute(MoviesDestination::class) == true ||
        destination?.hasRoute(ListsDestination::class) == true ||
        destination?.hasRoute(ProfileDestination::class) == true
}
