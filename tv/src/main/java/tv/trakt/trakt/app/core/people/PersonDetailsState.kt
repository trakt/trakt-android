package tv.trakt.trakt.app.core.people

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.model.Person
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class PersonDetailsState(
    val isLoading: Boolean = false,
    val personDetails: Person? = null,
    val personBackdropUrl: String? = null,
    val personShowCredits: ImmutableList<Show>? = null,
    val personMovieCredits: ImmutableList<Movie>? = null,
    val error: Exception? = null,
)
