package tv.trakt.trakt.app.core.main.navigation

import androidx.navigation.NavController
import tv.trakt.trakt.app.core.auth.navigation.navigateToAuth
import tv.trakt.trakt.app.core.discover.navigation.DiscoverDestination
import tv.trakt.trakt.app.core.discover.navigation.navigateToDiscover
import tv.trakt.trakt.app.core.home.navigation.HomeDestination
import tv.trakt.trakt.app.core.home.navigation.navigateToHome
import tv.trakt.trakt.app.core.lists.navigation.ListsDestination
import tv.trakt.trakt.app.core.lists.navigation.navigateToLists
import tv.trakt.trakt.app.core.search.navigation.SearchDestination
import tv.trakt.trakt.app.core.search.navigation.navigateToSearch

internal fun NavController.navigateToMainDestination(
    destination: Any,
    isSignedIn: Boolean,
) {
    when (destination) {
        HomeDestination -> navigateToHome()

        SearchDestination -> when {
            isSignedIn -> navigateToSearch()
            else -> navigateToAuth()
        }

        DiscoverDestination -> when {
            isSignedIn -> navigateToDiscover()
            else -> navigateToAuth()
        }

        ListsDestination -> when {
            isSignedIn -> navigateToLists()
            else -> navigateToAuth()
        }
    }
}
