package tv.trakt.trakt.core.trivia.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.trivia.TriviaScreen
import tv.trakt.trakt.core.trivia.TriviaViewModel

@Serializable
internal data class TriviaDestination(
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaImage: String?,
    val mediaTitle: String?,
    val navSource: String,
)

internal fun NavGraphBuilder.triviaScreen(onNavigateBack: () -> Unit) {
    composable<TriviaDestination> { entry ->
        val dest = entry.toRoute<TriviaDestination>()
        TriviaScreen(
            viewModel = koinViewModel<TriviaViewModel>(
                parameters = {
                    parametersOf(
                        TraktId(dest.mediaId),
                        dest.mediaType,
                        dest.mediaImage,
                        dest.mediaTitle,
                    )
                },
            ),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToTrivia(
    mediaId: TraktId,
    mediaType: MediaType,
    mediaImage: String?,
    mediaTitle: String?,
    navSource: String,
) {
    navigate(
        route = TriviaDestination(
            mediaId = mediaId.value,
            mediaType = mediaType,
            mediaImage = mediaImage,
            mediaTitle = mediaTitle,
            navSource = navSource,
        ),
    )
}
