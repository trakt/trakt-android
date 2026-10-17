package tv.trakt.trakt.common.firebase.analytics.implementation.hal.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private const val MAX_STORED_EVENTS = 500

internal class HalEventStore {
    private val events = MutableStateFlow(emptyList<HalEvent>())

    fun add(event: HalEvent) {
        events.update { current -> (current + event).takeLast(MAX_STORED_EVENTS) }
    }

    fun peek(limit: Int): List<HalEvent> {
        return events.value.take(limit)
    }

    fun remove(batch: List<HalEvent>) {
        events.update { current ->
            if (current.take(batch.size) == batch) {
                current.drop(batch.size)
            } else {
                current.filterNot(batch.toSet()::contains)
            }
        }
    }
}
