package tv.trakt.trakt.core.summary.shows.features.trivia

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.trivia.TriviaFact

@Immutable
internal data class ShowTriviaState(
    val user: User? = null,
    val summary: ImmutableList<String>? = null,
    val items: ImmutableList<TriviaFact>? = null,
    val collapsed: Boolean? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
