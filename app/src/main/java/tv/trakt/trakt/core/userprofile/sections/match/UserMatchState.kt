package tv.trakt.trakt.core.userprofile.sections.match

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserMatch

@Immutable
internal data class UserMatchState(
    val viewer: User? = null,
    val match: UserMatch? = null,
    val sharedMovies: ImmutableList<Movie>? = null,
    val sharedShows: ImmutableList<Show>? = null,
    val loading: LoadingState = LoadingState.Idle,
)
