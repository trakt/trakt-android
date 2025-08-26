package tv.trakt.trakt.app.core.streamings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.app.core.streamings.AllStreamingsScreen
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class AllStreamingsDestination(
    val mediaId: Int,
    val mediaType: String,
    val season: Int? = null,
    val episode: Int? = null,
) {
    init {
        require(mediaType in arrayOf("show", "movie", "episode")) {
            "Unsupported media type: $mediaType"
        }
    }
}

internal fun NavGraphBuilder.allStreamingsScreen() {
    composable<AllStreamingsDestination> {
        AllStreamingsScreen(
            viewModel = koinViewModel(),
        )
    }
}

internal fun NavController.navigateToShowStreamings(mediaId: TraktId) {
    navigate(
        route = AllStreamingsDestination(
            mediaId = mediaId.value,
            mediaType = "show",
        ),
    )
}

internal fun NavController.navigateToMovieStreamings(mediaId: TraktId) {
    navigate(
        route = AllStreamingsDestination(
            mediaId = mediaId.value,
            mediaType = "movie",
        ),
    )
}

internal fun NavController.navigateToEpisodeStreamings(
    mediaId: TraktId,
    seasonEpisode: SeasonEpisode,
) {
    navigate(
        route = AllStreamingsDestination(
            mediaId = mediaId.value,
            mediaType = "episode",
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        ),
    )
}
