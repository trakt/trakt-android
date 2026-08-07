package tv.trakt.trakt.core.calendar.feature.monthly.ui

import java.time.YearMonth

// Bounded pager standing in for an infinite one: 100 years either side of the
// anchor month, well past anything the calendar API serves.
internal const val MONTH_PAGE_COUNT = 2401
internal const val INITIAL_MONTH_PAGE = MONTH_PAGE_COUNT / 2

/** Month rendered on [page], with the receiver pinned to [INITIAL_MONTH_PAGE]. */
internal fun YearMonth.monthForPage(page: Int): YearMonth {
    return plusMonths((page - INITIAL_MONTH_PAGE).toLong())
}
