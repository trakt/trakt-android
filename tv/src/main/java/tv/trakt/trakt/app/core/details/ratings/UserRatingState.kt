package tv.trakt.trakt.app.core.details.ratings

import androidx.compose.runtime.Immutable

@Immutable
internal data class UserRatingState(
    val rating: Int? = null,
    val loading: Boolean = false,
    val watched: Boolean = false,
)
