package tv.trakt.trakt.core.ratings.rateprompt.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

sealed interface RatePromptState {
    object Idle : RatePromptState

    object Loading : RatePromptState

    object AskSuppress : RatePromptState

    data class UnratedMovies(
        val movies: ImmutableList<Movie>,
        val favorites: ImmutableSet<TraktId>,
    ) : RatePromptState

    fun isActive(): Boolean {
        return when {
            this is UnratedMovies -> true
            else -> false
        }
    }
}
