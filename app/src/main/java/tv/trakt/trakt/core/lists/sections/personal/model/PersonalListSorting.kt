package tv.trakt.trakt.core.lists.sections.personal.model

import tv.trakt.trakt.common.model.sorting.SortOrder.ASCENDING
import tv.trakt.trakt.common.model.sorting.SortOrder.DESCENDING
import tv.trakt.trakt.common.model.sorting.SortTypeList.ADDED
import tv.trakt.trakt.common.model.sorting.SortTypeList.DEFAULT
import tv.trakt.trakt.common.model.sorting.SortTypeList.RATING
import tv.trakt.trakt.common.model.sorting.SortTypeList.RELEASED
import tv.trakt.trakt.common.model.sorting.SortTypeList.RUNTIME
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.PersonalListItem

internal fun getPersonalListSorting(sort: Sorting?): Comparator<PersonalListItem> {
    if (sort == null) {
        // Defaults to recently added.
        return compareByDescending { it.listedAt }
    }
    return when (sort.type) {
        DEFAULT -> when (sort.order) {
            ASCENDING -> compareBy { it.rank }
            DESCENDING -> compareByDescending { it.rank }
        }

        ADDED -> when (sort.order) {
            ASCENDING -> compareBy { it.listedAt }
            DESCENDING -> compareByDescending { it.listedAt }
        }

        RUNTIME -> when (sort.order) {
            ASCENDING -> compareBy { it.runtime }
            DESCENDING -> compareByDescending { it.runtime }
        }

        RELEASED -> when (sort.order) {
            ASCENDING -> compareBy { it.released }
            DESCENDING -> compareByDescending { it.released }
        }

        RATING -> when (sort.order) {
            ASCENDING -> compareBy { it.rating.rating }
            DESCENDING -> compareByDescending { it.rating.rating }
        }
    }
}
