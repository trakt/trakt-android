package tv.trakt.trakt.core.summary.credits.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.credits.AllCreditsScreen
import tv.trakt.trakt.core.summary.credits.model.CreditsSource
import tv.trakt.trakt.core.summary.people.model.PersonCreditsRole

/**
 * [mediaId] is the movie or show id. For episodes it is the parent show id.
 */
@Serializable
internal data class AllCreditsDestination(
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaTitle: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val backgroundUrl: String? = null,
) {
    fun toSource(): CreditsSource {
        return when (mediaType) {
            MediaType.Movie -> CreditsSource.Movie(mediaId.toTraktId())
            MediaType.Show -> CreditsSource.Show(mediaId.toTraktId())
            MediaType.Episode -> CreditsSource.Episode(
                showId = mediaId.toTraktId(),
                season = requireNotNull(season) { "Episode credits require a season" },
                episode = requireNotNull(episode) { "Episode credits require an episode" },
            )
            MediaType.Season -> error("Season credits are not supported")
        }
    }
}

internal fun NavGraphBuilder.allCreditsScreen(
    onNavigateToPerson: (AllCreditsDestination, Person, PersonCreditsRole?) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllCreditsDestination> { entry ->
        val destination = entry.toRoute<AllCreditsDestination>()
        AllCreditsScreen(
            viewModel = koinViewModel(),
            onPersonClick = { person, role ->
                onNavigateToPerson(destination, person, role)
            },
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllCredits(
    mediaId: TraktId,
    mediaType: MediaType,
    mediaTitle: String?,
    backgroundUrl: String?,
    season: Int? = null,
    episode: Int? = null,
) {
    navigate(
        route = AllCreditsDestination(
            mediaId = mediaId.value,
            mediaType = mediaType,
            mediaTitle = mediaTitle,
            season = season,
            episode = episode,
            backgroundUrl = backgroundUrl,
        ),
    )
}
