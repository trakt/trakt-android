package tv.trakt.trakt.core.mixtape.results

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.model.MIXTAPE_SLOT_COUNT
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget

@Immutable
internal data class MixtapeResultsState(
    val type: MediaType = MediaType.Show,
    val loading: Boolean = true,
    val suggestions: ImmutableList<Suggestion> = persistentListOf(),
    val slots: ImmutableList<Suggestion> = persistentListOf(),
    val rounds: ImmutableList<ImmutableList<MixtapeItem>> = persistentListOf(persistentListOf()),
    val pickTitles: ImmutableMap<TraktId, String> = persistentMapOf(),
    val savedTo: MixtapeTarget = MixtapeTarget.Watchlist,
    val lists: ImmutableList<MixtapeTarget.PersonalList> = persistentListOf(),
    val moving: Boolean = false,
    val failed: ImmutableSet<TraktId> = persistentSetOf(),
    val error: Exception? = null,
    val swipeHint: Boolean = false,
) {
    val round: ImmutableList<MixtapeItem>
        get() = rounds.lastOrNull() ?: persistentListOf()

    val position: Int
        get() = round.size

    val current: Suggestion?
        get() = slots.getOrNull(position)

    val next: Suggestion?
        get() = slots.getOrNull(position + 1)

    val isFirstRound: Boolean
        get() = rounds.size <= 1

    val keptRounds: List<List<MixtapeItem>>
        get() = rounds.map { round -> round.filterNot { it.id in failed } }

    val keptInRound: Int
        get() = round.count { it.id !in failed }

    val keptSoFar: Int
        get() = keptRounds.sumOf { it.size }

    val isEmpty: Boolean
        get() = !loading && error == null && keptSoFar == 0 && slots.isEmpty()

    val isDone: Boolean
        get() = !loading && (position >= MIXTAPE_SLOT_COUNT || current == null)
}
