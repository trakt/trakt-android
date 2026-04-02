package tv.trakt.trakt.core.trivia

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.core.trivia.model.TriviaFilter

@Immutable
internal data class TriviaState(
    val user: User? = null,
    val items: ImmutableList<TriviaFact>? = null,
    val filteredItems: ImmutableList<TriviaFact>? = null,
    val filter: TriviaFilter? = TriviaFilter.NoSpoilers,
    val backgroundUrl: String? = null,
    val mediaTitle: String? = null,
    val error: Exception? = null,
)
