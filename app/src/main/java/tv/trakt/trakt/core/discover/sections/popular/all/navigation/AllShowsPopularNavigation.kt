package tv.trakt.trakt.core.discover.sections.popular.all.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.sections.popular.all.AllShowsPopularScreen

@Serializable
internal data object ShowsPopularDestination

internal fun NavGraphBuilder.showsPopularScreen(
    context: Context?,
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
) {
    val halloween = (context as? MainActivity)?.halloweenConfig?.enabled == true
    composable<ShowsPopularDestination> {
        AllShowsPopularScreen(
            viewModel = koinViewModel {
                parametersOf(halloween)
            },
            onNavigateToShow = onNavigateToShow,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToPopularShows() {
    navigate(route = ShowsPopularDestination)
}
