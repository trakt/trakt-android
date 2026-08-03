package tv.trakt.trakt.core.summary.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.summary.shows.ShowDetailsScreen

@Serializable
internal data class ShowDetailsDestination(
    val showId: Int,
)

internal fun NavGraphBuilder.showDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToComments: (Show, CommentsFilter) -> Unit,
    onNavigateToList: (Show, CustomList) -> Unit,
    onNavigateToPerson: (Show, Person) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToTrivia: (Show) -> Unit,
    onNavigateToSentiment: (Show, Sentiments) -> Unit,
    onNavigateToTrailer: (String) -> Unit,
    onNavigateToExtra: (ExtraVideo) -> Unit,
    onNavigateToAllSeasons: (Show, Int?) -> Unit,
    onNavigateToHistory: (Show, Int) -> Unit,
    onNavigateToUser: (User) -> Unit,
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<ShowDetailsDestination> {
        ShowDetailsScreen(
            viewModel = koinViewModel(),
            onShowClick = { onNavigateToShow(it.ids.trakt) },
            onEpisodeClick = { showId, episode -> onNavigateToEpisode(showId, episode) },
            onCommentsClick = { show, filter ->
                onNavigateToComments(show, filter)
            },
            onListClick = { show, list -> onNavigateToList(show, list) },
            onPersonClick = { show, person -> onNavigateToPerson(show, person) },
            onTriviaClick = { show -> onNavigateToTrivia(show) },
            onSentimentClick = { show, sentiments -> onNavigateToSentiment(show, sentiments) },
            onTrailerClick = { url -> onNavigateToTrailer(url) },
            onExtraClick = { extra -> onNavigateToExtra(extra) },
            onAllSeasonsClick = onNavigateToAllSeasons,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToUser = onNavigateToUser,
            onNavigateVip = onNavigateVip,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToShow(showId: TraktId) {
    navigate(route = ShowDetailsDestination(showId.value))
}
