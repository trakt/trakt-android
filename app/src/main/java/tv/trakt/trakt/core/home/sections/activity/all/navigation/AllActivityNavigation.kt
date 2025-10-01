package tv.trakt.trakt.core.home.sections.activity.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.all.personal.AllActivityPersonalScreen
import tv.trakt.trakt.core.home.sections.activity.all.social.AllActivitySocialScreen

@Serializable
internal data object AllPersonalActivityDestination

@Serializable
internal data object AllSocialActivityDestination

internal fun NavGraphBuilder.homeActivityPersonalScreen(
    onNavigateBack: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    composable<AllPersonalActivityDestination> {
        AllActivityPersonalScreen(
            onNavigateBack = onNavigateBack,
            onMovieClick = onMovieClick,
        )
    }
}

internal fun NavGraphBuilder.homeActivitySocialScreen(
    onNavigateBack: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    composable<AllSocialActivityDestination> {
        AllActivitySocialScreen(
            onNavigateBack = onNavigateBack,
            onMovieClick = onMovieClick,
        )
    }
}

internal fun NavController.navigateToAllActivityPersonal() {
    navigate(route = AllPersonalActivityDestination)
}

internal fun NavController.navigateToAllActivitySocial() {
    navigate(route = AllSocialActivityDestination)
}
