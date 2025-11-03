package tv.trakt.trakt.core.summary.episodes

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating

@Immutable
internal data class EpisodeDetailsState(
    val show: Show? = null,
    val episode: Episode? = null,
    val episodeRatings: ExternalRating? = null,
    val episodeUserRating: UserRatingsState? = null,
    val episodeProgress: ProgressState? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingProgress: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
    val user: User? = null,
) {
    data class ProgressState(
        val plays: Int?,
    )

    data class UserRatingsState(
        val rating: UserRating? = null,
        val loading: LoadingState = LoadingState.IDLE,
    )
}
