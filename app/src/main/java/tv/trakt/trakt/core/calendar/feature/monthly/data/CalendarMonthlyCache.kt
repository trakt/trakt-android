package tv.trakt.trakt.core.calendar.feature.monthly.data

import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItems
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.YearMonth

private const val MAX_CACHED_MONTHS = 12

internal class CalendarMonthlyItemsCache {
    private val entries = LinkedHashMap<CalendarMonthKey, CalendarItems>()

    fun get(key: CalendarMonthKey): CalendarItems? {
        val items = entries.remove(key) ?: return null
        entries[key] = items
        return items
    }

    fun put(
        key: CalendarMonthKey,
        items: CalendarItems,
    ) {
        entries.remove(key)
        entries[key] = items

        while (entries.size > MAX_CACHED_MONTHS) {
            entries.remove(entries.keys.first())
        }
    }

    /** Applies an optimistic patch - a watched toggle - to every cached month. */
    fun patch(transform: (CalendarItems) -> CalendarItems) {
        entries.replaceAll { _, items -> transform(items) }
    }

    fun clear() {
        entries.clear()
    }
}

internal data class CalendarMonthKey(
    val month: YearMonth,
    val filter: GlobalFilter?,
    val type: ReleaseType,
)
