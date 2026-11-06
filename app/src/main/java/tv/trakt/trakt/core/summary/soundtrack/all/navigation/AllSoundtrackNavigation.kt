package tv.trakt.trakt.core.summary.soundtrack.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.soundtrack.all.AllSoundtrackScreen

@Serializable
internal data class AllSoundtrackDestination(
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaTitle: String,
    val posterUrl: String? = null,
    val backgroundUrl: String? = null,
    val currentSeason: Int? = null,
)

internal fun NavGraphBuilder.allSoundtrackScreen(onNavigateBack: () -> Unit) {
    composable<AllSoundtrackDestination> {
        AllSoundtrackScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllSoundtrack(
    mediaId: TraktId,
    mediaType: MediaType,
    mediaTitle: String,
    posterUrl: String?,
    backgroundUrl: String?,
    currentSeason: Int? = null,
) {
    navigate(
        route = AllSoundtrackDestination(
            mediaId = mediaId.value,
            mediaType = mediaType,
            mediaTitle = mediaTitle,
            posterUrl = posterUrl,
            backgroundUrl = backgroundUrl,
            currentSeason = currentSeason,
        ),
    )
}
