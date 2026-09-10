package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsPeopleMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons

@Immutable
internal data class AllShowSeasonsState(
    val show: Show? = null,
    val user: User? = null,
    val mode: SeasonsMode = SeasonsMode.Episodes,
    val peopleMode: SeasonsPeopleMode = SeasonsPeopleMode.Cast,
    val commentsMode: CommentsFilter = CommentsFilter.Popular,
    val commentReactions: ImmutableMap<Int, ReactionsSummary> = persistentMapOf(),
    val userReactions: ImmutableMap<Int, Reaction?> = persistentMapOf(),
    val seasonUserRating: UserRatingState = UserRatingState(),
    val backgroundUrl: String? = null,
    val items: ShowSeasons = ShowSeasons(),
    val loading: LoadingState = LoadingState.Idle,
    val loadingEpisode: LoadingState = LoadingState.Idle,
    val loadingSeason: LoadingState = LoadingState.Idle,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val watchedUntilPrompt: Episode? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
) {
    @Immutable
    data class UserRatingState(
        val rating: UserRating? = null,
        val loading: LoadingState = LoadingState.Idle,
    )
}
