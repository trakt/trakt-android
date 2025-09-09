package tv.trakt.trakt.app.core.details.movie

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.model.CastPerson
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.common.model.ExternalRating
import tv.trakt.trakt.app.common.model.ExtraVideo
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.helpers.StringResource
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.User

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
    val isReviewRequest: Boolean = false,
    val snackMessage: StringResource? = null,
) {
    @Immutable
    internal data class StreamingsState(
        val slug: SlugId? = null,
        val service: StreamingService? = null,
        val noServices: Boolean = false,
        val plex: Boolean = false,
        val loading: Boolean = false,
        val info: StringResource? = null,
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
