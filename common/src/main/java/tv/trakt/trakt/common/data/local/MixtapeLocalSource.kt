package tv.trakt.trakt.common.data.local

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion

/**
 * Hands suggestions preloaded on the picker over to the results screen.
 * In-memory only, holds the latest mix and is consumed once.
 */
class MixtapeLocalSource {
    private data class Entry(
        val type: MediaType,
        val picks: List<TraktId>,
        val suggestions: ImmutableList<Suggestion>,
    )

    private val storage = MutableStateFlow<Entry?>(null)

    fun setSuggestions(
        type: MediaType,
        picks: List<TraktId>,
        suggestions: ImmutableList<Suggestion>,
    ) {
        storage.update { Entry(type, picks, suggestions) }
    }

    fun takeSuggestions(
        type: MediaType,
        picks: List<TraktId>,
    ): ImmutableList<Suggestion>? {
        fun Entry?.matches() = this != null && this.type == type && this.picks == picks

        val entry = storage.getAndUpdate { if (it.matches()) null else it }
        return entry?.takeIf { it.matches() }?.suggestions
    }
}
