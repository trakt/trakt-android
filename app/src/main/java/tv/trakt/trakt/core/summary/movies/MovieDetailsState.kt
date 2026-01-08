package tv.trakt.trakt.core.summary.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating

@Immutable
internal data class MovieDetailsState(
    val movie: Movie? = null,
    val movieRatings: ExternalRating? = null,
    val movieUserRating: UserRatingsState? = null,
    val movieStudios: ImmutableList<String>? = null,
    val movieProgress: ProgressState? = null,
    val movieCreator: Person? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingProgress: LoadingState = LoadingState.IDLE,
    val loadingLists: LoadingState = LoadingState.IDLE,
    val loadingFavorite: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
    val user: User? = null,
    val metaCollapsed: Boolean? = null,
) {
    data class ProgressState(
        val plays: Int,
        val inWatchlist: Boolean,
        val inLists: Boolean,
        val hasLists: Boolean,
    ) {
        val inAnyList: Boolean = inWatchlist || inLists
    }

    data class UserRatingsState(
        val rating: UserRating? = null,
        val loading: LoadingState = LoadingState.IDLE,
    )
}
