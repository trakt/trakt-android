package tv.trakt.trakt.core.summary.people

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class PersonDetailsState(
    val user: User? = null,
    val personDetails: Person? = null,
    val personBackdropUrl: String? = null,
    val personShowCredits: ImmutableMap<String, ImmutableList<PersonCreditItem.ShowItem>>? = null,
    val personMovieCredits: ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val loadingDetails: LoadingState = LoadingState.IDLE,
    val loadingCredits: LoadingState = LoadingState.IDLE,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val error: Exception? = null,
)
