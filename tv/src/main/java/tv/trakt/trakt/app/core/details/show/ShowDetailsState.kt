package tv.trakt.trakt.app.core.details.show

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.model.CastPerson
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.common.model.ExternalRating
import tv.trakt.trakt.app.common.model.ExtraVideo
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.details.show.models.ShowSeasons
import tv.trakt.trakt.app.helpers.StringResource
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ShowDetailsState(
    val user: User? = null,
    val showDetails: Show? = null,
    val showRatings: ExternalRating? = null,
    val showVideos: ImmutableList<ExtraVideo>? = null,
    val showCast: ImmutableList<CastPerson>? = null,
    val showRelated: ImmutableList<Show>? = null,
    val showComments: ImmutableList<Comment>? = null,
    val showLists: ImmutableList<CustomList>? = null,
    val showSeasons: ShowSeasons = ShowSeasons(),
    val showStreamings: StreamingsState = StreamingsState(),
    val showCollection: CollectionState = CollectionState(),
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
        val isWatchedLoading: Boolean = false,
        val isWatchlistLoading: Boolean = false,
        val isWatched: Boolean = false,
        val isWatchlist: Boolean = false,
        val episodesPlays: Int = 0,
        val episodesAiredCount: Int = 0,
    ) {
        val isLoading: Boolean
            get() = isWatchedLoading || isWatchlistLoading

        val isAllWatched: Boolean
            get() = episodesPlays > 0 && episodesPlays >= episodesAiredCount
    }
}
