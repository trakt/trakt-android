package tv.trakt.trakt.core.library.model

import tv.trakt.trakt.common.model.sorting.SortOrder.ASCENDING
import tv.trakt.trakt.common.model.sorting.SortOrder.DESCENDING
import tv.trakt.trakt.common.model.sorting.SortTypeList.ADDED
import tv.trakt.trakt.common.model.sorting.SortTypeList.DEFAULT
import tv.trakt.trakt.common.model.sorting.SortTypeList.RATING
import tv.trakt.trakt.common.model.sorting.SortTypeList.RELEASED
import tv.trakt.trakt.common.model.sorting.SortTypeList.RUNTIME
import tv.trakt.trakt.common.model.sorting.Sorting

internal fun getLibrarySorting(sort: Sorting?): Comparator<LibraryItem> {
    if (sort == null) {
        // Defaults to recently collected.
        return compareByDescending { it.collectedAt }
    }
    return when (sort.type) {
        DEFAULT -> when (sort.order) {
            // For library, default sorting is by collected date
            ASCENDING -> compareBy { it.collectedAt }

            DESCENDING -> compareByDescending { it.collectedAt }
        }

        ADDED -> when (sort.order) {
            ASCENDING -> compareBy { it.collectedAt }
            DESCENDING -> compareByDescending { it.collectedAt }
        }

        RUNTIME -> when (sort.order) {
            ASCENDING -> compareBy {
                when (it) {
                    is LibraryItem.EpisodeItem -> it.episode.runtime
                    is LibraryItem.MovieItem -> it.movie.runtime
                }
            }

            DESCENDING -> compareByDescending {
                when (it) {
                    is LibraryItem.EpisodeItem -> it.episode.runtime
                    is LibraryItem.MovieItem -> it.movie.runtime
                }
            }
        }

        RELEASED -> when (sort.order) {
            ASCENDING -> compareBy {
                when (it) {
                    is LibraryItem.EpisodeItem -> it.episode.firstAired
                    is LibraryItem.MovieItem -> it.movie.released?.atStartOfDay()
                }
            }

            DESCENDING -> compareByDescending {
                when (it) {
                    is LibraryItem.EpisodeItem -> it.episode.firstAired
                    is LibraryItem.MovieItem -> it.movie.released?.atStartOfDay()
                }
            }
        }

        RATING -> when (sort.order) {
            ASCENDING -> compareBy {
                when (it) {
                    is LibraryItem.EpisodeItem -> it.episode.rating.rating
                    is LibraryItem.MovieItem -> it.movie.rating.rating
                }
            }

            DESCENDING -> compareByDescending {
                when (it) {
                    is LibraryItem.EpisodeItem -> it.episode.rating.rating
                    is LibraryItem.MovieItem -> it.movie.rating.rating
                }
            }
        }
    }
}
