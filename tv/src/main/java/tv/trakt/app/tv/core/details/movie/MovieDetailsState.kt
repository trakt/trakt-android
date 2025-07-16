package tv.trakt.app.tv.core.details.movie

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.common.model.CastPerson
import tv.trakt.app.tv.common.model.Comment
import tv.trakt.app.tv.common.model.CustomList
import tv.trakt.app.tv.common.model.ExternalRating
import tv.trakt.app.tv.common.model.ExtraVideo
import tv.trakt.app.tv.common.model.SlugId
import tv.trakt.app.tv.common.model.StreamingService
import tv.trakt.app.tv.common.model.User
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.helpers.StringResource

@Immutable
internal data class MovieDetailsState(
    val user: User? = null,
    val movieDetails: Movie? = null,
    val movieRatings: ExternalRating? = null,
    val movieVideos: ImmutableList<ExtraVideo>? = null,
    val movieCast: ImmutableList<CastPerson>? = null,
    val movieRelated: ImmutableList<Movie>? = null,
    val movieComments: ImmutableList<Comment>? = null,
    val movieLists: ImmutableList<CustomList>? = null,
    val movieStreamings: StreamingsState = StreamingsState(),
    val movieCollection: CollectionState = CollectionState(),
    val isLoading: Boolean = false,
    val snackMessage: StringResource? = null,
) {
    @Immutable
    internal data class StreamingsState(
        val slug: SlugId? = null,
        val service: StreamingService? = null,
        val isLoading: Boolean = false,
    )

    @Immutable
    internal data class CollectionState(
        val isHistoryLoading: Boolean = false,
        val isWatchlistLoading: Boolean = false,
        val isHistory: Boolean = false,
        val isWatchlist: Boolean = false,
        val historyCount: Int = 0,
    ) {
        val isLoading: Boolean
            get() = isHistoryLoading || isWatchlistLoading
    }
}
