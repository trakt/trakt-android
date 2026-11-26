package tv.trakt.trakt.core.mixtape.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion

internal const val MIXTAPE_SLOT_COUNT = 3

private data class SlotState(
    val slots: List<Suggestion>,
    val queue: List<Suggestion>,
)

internal fun toMixtapeSlots(
    suggestions: List<Suggestion>,
    skips: List<TraktId>,
): ImmutableList<Suggestion> {
    val initial = SlotState(
        slots = suggestions.take(MIXTAPE_SLOT_COUNT).sortedByDescending { it.match },
        queue = suggestions.drop(MIXTAPE_SLOT_COUNT),
    )
    return skips
        .fold(initial, ::replaceSkipped)
        .slots
        .toImmutableList()
}

private fun replaceSkipped(
    state: SlotState,
    id: TraktId,
): SlotState {
    val index = state.slots.indexOfFirst { it.item.id == id }
    if (index < 0) return state

    val next = state.queue.firstOrNull()
        ?: return state.copy(slots = state.slots.filterIndexed { slot, _ -> slot != index })

    return SlotState(
        slots = state.slots.mapIndexed { slot, suggestion -> if (slot == index) next else suggestion },
        queue = state.queue.drop(1),
    )
}
