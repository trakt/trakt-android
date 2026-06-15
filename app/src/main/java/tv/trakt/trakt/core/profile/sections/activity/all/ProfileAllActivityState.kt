package tv.trakt.trakt.core.profile.sections.activity.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem

@Immutable
internal data class ProfileAllActivityState(
    val filter: ProfileActivityFilter? = null,
    val ratingItems: ImmutableList<ProfileRatingItem>? = null,
    val commentItems: ImmutableList<ProfileCommentItem>? = null,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val user: User? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
