package tv.trakt.trakt.core.streamings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.streamings.AllStreamingsScreen

@Serializable
internal data class AllStreamingsDestination(
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaTitle: String? = null,
    val backgroundUrl: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
) {
    init {
        require(mediaType != MediaType.Season) {
            "Unsupported media type: $mediaType"
        }
    }
}

internal fun NavGraphBuilder.allStreamingsScreen(onNavigateBack: () -> Unit) {
    composable<AllStreamingsDestination> {
        AllStreamingsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToShowStreamings(
    showId: TraktId,
    showTitle: String,
    backgroundUrl: String? = null,
) {
    navigate(
        route = AllStreamingsDestination(
            mediaId = showId.value,
            mediaType = MediaType.Show,
            mediaTitle = showTitle,
            backgroundUrl = backgroundUrl,
        ),
    )
}

internal fun NavController.navigateToMovieStreamings(
    movieId: TraktId,
    movieTitle: String,
    backgroundUrl: String? = null,
) {
    navigate(
        route = AllStreamingsDestination(
            mediaId = movieId.value,
            mediaType = MediaType.Movie,
            mediaTitle = movieTitle,
            backgroundUrl = backgroundUrl,
        ),
    )
}

internal fun NavController.navigateToEpisodeStreamings(
    showId: TraktId,
    episodeTitle: String,
    seasonEpisode: SeasonEpisode,
    backgroundUrl: String? = null,
) {
    navigate(
        route = AllStreamingsDestination(
            mediaId = showId.value,
            mediaType = MediaType.Episode,
            mediaTitle = episodeTitle,
            backgroundUrl = backgroundUrl,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        ),
    )
}
