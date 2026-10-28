package tv.trakt.trakt.core.inreview.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.inreview.InReviewScreen

@Serializable
internal data class InReviewDestination(
    val url: String,
)

internal fun NavController.navigateToInReview(url: String) {
    navigate(
        route = InReviewDestination(
            url = url,
        ),
    )
}

internal fun NavGraphBuilder.inReviewScreen(onNavigateBack: () -> Unit) {
    composable<InReviewDestination> {
        InReviewScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}
