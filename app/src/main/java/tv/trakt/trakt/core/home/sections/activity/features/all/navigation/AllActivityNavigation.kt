package tv.trakt.trakt.core.home.sections.activity.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.features.all.personal.AllActivityPersonalScreen
import tv.trakt.trakt.core.home.sections.activity.features.all.social.AllActivitySocialScreen

@Serializable
internal data class AllPersonalActivityDestination(
    val filtersEnabled: Boolean,
)

@Serializable
internal data object AllSocialActivityDestination

internal fun NavGraphBuilder.homeActivityPersonalScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllPersonalActivityDestination> {
        AllActivityPersonalScreen(
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavGraphBuilder.homeActivitySocialScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllSocialActivityDestination> {
        AllActivitySocialScreen(
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllActivityPersonal(filtersEnabled: Boolean) {
    navigate(
        route = AllPersonalActivityDestination(
            filtersEnabled = filtersEnabled,
        ),
    )
}

internal fun NavController.navigateToAllActivitySocial() {
    navigate(route = AllSocialActivityDestination)
}
