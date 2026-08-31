package tv.trakt.trakt.core.summary.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity

@Immutable
internal data class ShowDetailsState(
    val show: Show? = null,
    val showRatings: ExternalRating? = null,
    val showUserRating: UserRatingsState? = null,
    val showProgress: ProgressState? = null,
    val showCreator: Person? = null,
    val showTranslation: MediaTranslation? = null,
    val showSocials: ImmutableList<MediaSocialActivity>? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingProgress: LoadingState = LoadingState.Idle,
    val loadingLists: LoadingState = LoadingState.Idle,
    val loadingFavorite: LoadingState = LoadingState.Idle,
    val info: StringResource? = null,
    val error: Exception? = null,
    val user: User? = null,
) {
    data class ProgressState(
        val aired: Int,
        val plays: Int?,
        val playsWithoutSpecials: Int?,
        val inWatchlist: Boolean,
        val inLists: Boolean,
    ) {
        val isWatched: Boolean
            get() = playsWithoutSpecials != null && playsWithoutSpecials > 0 && playsWithoutSpecials >= aired

        val isWatching: Boolean
            get() = playsWithoutSpecials != null && playsWithoutSpecials > 0 && playsWithoutSpecials < aired
    }

    data class UserRatingsState(
        val rating: UserRating? = null,
        val loading: LoadingState = LoadingState.Idle,
    )
}
