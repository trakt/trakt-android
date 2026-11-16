package tv.trakt.trakt.app.core.search.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.app.core.search.SearchScreen
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList

@Serializable
internal data object SearchDestination

internal fun NavGraphBuilder.searchScreen(
    onNavigateToShow: (showId: TraktId) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
) {
    composable<SearchDestination> {
        SearchScreen(
            viewModel = koinViewModel(),
            backgroundUrl = remember { Firebase.remoteConfig.getString(BACKGROUND_IMAGE_URL) },
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onPersonClick = onNavigateToPerson,
            onListClick = onNavigateToList,
        )
    }
}

internal fun NavController.navigateToSearch() {
    navigate(route = SearchDestination) {
        popUpToTop(this@navigateToSearch)
    }
}
