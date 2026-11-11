package tv.trakt.trakt.core.summary.movies.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.summary.movies.MovieDetailsScreen
import tv.trakt.trakt.core.summary.people.model.PersonCreditsRole
import tv.trakt.trakt.core.summary.sentiment.model.MediaInsights

@Serializable
internal data class MovieDetailsDestination(
    val movieId: Int,
)

internal fun NavGraphBuilder.movieDetailsScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToComments: (Movie, CommentsFilter) -> Unit,
    onNavigateToPerson: (Movie, Person, PersonCreditsRole) -> Unit,
    onNavigateToList: (Movie, CustomList) -> Unit,
    onNavigateToTrivia: (Movie) -> Unit,
    onNavigateToPersonLink: (Movie, SlugId) -> Unit,
    onNavigateToSentiment: (Movie, MediaInsights) -> Unit,
    onNavigateToTrailer: (String) -> Unit,
    onNavigateToExtra: (ExtraVideo) -> Unit,
    onNavigateToHistory: (Movie, watched: Int) -> Unit,
    onNavigateToAllStreamings: (Movie) -> Unit,
    onNavigateToAllCredits: (Movie) -> Unit,
    onNavigateToAllSoundtrack: (Movie) -> Unit,
    onNavigateToUser: (User) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateVip: (VipUpsellSource) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<MovieDetailsDestination> {
        MovieDetailsScreen(
            viewModel = koinViewModel(),
            onMovieClick = { onNavigateToMovie(it.ids.trakt) },
            onCommentsClick = { movie, filter ->
                onNavigateToComments(movie, filter)
            },
            onListClick = { movie, list -> onNavigateToList(movie, list) },
            onPersonClick = { movie, person, role -> onNavigateToPerson(movie, person, role) },
            onTriviaClick = { movie -> onNavigateToTrivia(movie) },
            onPersonLinkClick = { movie, slug -> onNavigateToPersonLink(movie, slug) },
            onSentimentClick = { movie, insights -> onNavigateToSentiment(movie, insights) },
            onTrailerClick = { url -> onNavigateToTrailer(url) },
            onExtraClick = { extra -> onNavigateToExtra(extra) },
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToAllStreamings = onNavigateToAllStreamings,
            onNavigateToAllCredits = onNavigateToAllCredits,
            onNavigateToAllSoundtrack = onNavigateToAllSoundtrack,
            onNavigateToUser = onNavigateToUser,
            onNavigateToWatchlist = onNavigateToWatchlist,
            onNavigateVip = onNavigateVip,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToMovie(movieId: TraktId) {
    val currentMovieId = currentBackStackEntry?.arguments?.getInt("movieId")
    if (currentMovieId == movieId.value) {
        // Already on the movie details screen.
        return
    }
    navigate(route = MovieDetailsDestination(movieId.value))
}
