package tv.trakt.trakt.core.favorites.model

import tv.trakt.trakt.common.model.sorting.SortOrder.Asc
import tv.trakt.trakt.common.model.sorting.SortOrder.Desc
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Default
import tv.trakt.trakt.common.model.sorting.SortType.Rating
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting

internal fun getFavoriteSorting(sort: Sorting?): Comparator<FavoriteItem> {
    if (sort == null) {
        return compareByDescending { it.listedAt }
    }
    return when (sort.type) {
        Default -> when (sort.order) {
            Asc -> compareBy { it.rank }
            Desc -> compareByDescending { it.rank }
        }

        Added -> when (sort.order) {
            Asc -> compareBy { it.listedAt }
            Desc -> compareByDescending { it.listedAt }
        }

        Runtime -> when (sort.order) {
            Asc -> compareBy { it.runtime }
            Desc -> compareByDescending { it.runtime }
        }

        Released -> when (sort.order) {
            Asc -> compareBy { it.released }
            Desc -> compareByDescending { it.released }
        }

        Rating -> when (sort.order) {
            Asc -> compareBy { it.rating.rating }
            Desc -> compareByDescending { it.rating.rating }
        }

        UserRating -> when (sort.order) {
            Asc -> compareBy { it.userRating?.rating }
            Desc -> compareByDescending { it.userRating?.rating }
        }

        Title -> when (sort.order) {
            Asc -> compareBy { it.title }
            Desc -> compareByDescending { it.title }
        }
    }
}
