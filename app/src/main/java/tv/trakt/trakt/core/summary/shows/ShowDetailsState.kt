package tv.trakt.trakt.core.summary.shows

import androidx.compose.runtime.Immutable
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

@Immutable
internal data class ShowDetailsState(
    val show: Show? = null,
    val showRatings: ExternalRating? = null,
    val showUserRating: UserRatingsState? = null,
    val showProgress: ProgressState? = null,
    val showCreator: Person? = null,
    val showTranslation: MediaTranslation? = null,
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
        val inWatchlist: Boolean,
        val inLists: Boolean,
    ) {
        val isWatched: Boolean
            get() = plays != null && plays > 0
    }

    data class UserRatingsState(
        val rating: UserRating? = null,
        val loading: LoadingState = LoadingState.Idle,
    )
}
