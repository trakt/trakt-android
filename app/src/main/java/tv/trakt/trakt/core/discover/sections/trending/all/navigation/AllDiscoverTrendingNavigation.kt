package tv.trakt.trakt.core.discover.sections.trending.all.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.sections.trending.all.AllDiscoverTrendingScreen

@Serializable
internal data object DiscoverTrendingDestination

internal fun NavGraphBuilder.discoverTrendingScreen(
    context: Context?,
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val halloween = (context as? MainActivity)?.halloweenConfig?.enabled == true
    composable<DiscoverTrendingDestination> {
        AllDiscoverTrendingScreen(
            viewModel = koinViewModel {
                parametersOf(halloween)
            },
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToDiscoverTrending() {
    navigate(route = DiscoverTrendingDestination)
}
