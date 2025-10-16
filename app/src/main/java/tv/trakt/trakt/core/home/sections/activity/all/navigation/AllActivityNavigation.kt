package tv.trakt.trakt.core.home.sections.activity.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.all.personal.AllActivityPersonalScreen
import tv.trakt.trakt.core.home.sections.activity.all.social.AllActivitySocialScreen

@Serializable
internal data object AllPersonalActivityDestination

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

internal fun NavController.navigateToAllActivityPersonal() {
    navigate(route = AllPersonalActivityDestination)
}

internal fun NavController.navigateToAllActivitySocial() {
    navigate(route = AllSocialActivityDestination)
}
