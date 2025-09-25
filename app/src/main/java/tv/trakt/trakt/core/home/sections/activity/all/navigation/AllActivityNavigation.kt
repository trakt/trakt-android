package tv.trakt.trakt.core.home.sections.activity.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.core.home.sections.activity.all.personal.AllActivityPersonalScreen
import tv.trakt.trakt.core.home.sections.activity.all.social.AllActivitySocialScreen

@Serializable
internal data object AllPersonalActivityDestination

@Serializable
internal data object AllSocialActivityDestination

internal fun NavGraphBuilder.homeActivityPersonalScreen(onNavigateBack: () -> Unit) {
    composable<AllPersonalActivityDestination> {
        AllActivityPersonalScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavGraphBuilder.homeActivitySocialScreen(onNavigateBack: () -> Unit) {
    composable<AllSocialActivityDestination> {
        AllActivitySocialScreen(
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
