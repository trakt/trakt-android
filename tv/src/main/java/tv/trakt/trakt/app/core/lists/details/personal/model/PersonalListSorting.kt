package tv.trakt.trakt.app.core.lists.details.personal.model

import tv.trakt.trakt.common.model.sorting.SortOrder.Asc
import tv.trakt.trakt.common.model.sorting.SortOrder.Desc
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Rank
import tv.trakt.trakt.common.model.sorting.SortType.Rating
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration

internal fun getPersonalListSorting(sorting: Sorting): Comparator<PersonalListItem> {
    return when (sorting.type) {
        Rank -> when (sorting.order) {
            Asc -> compareBy { it.rank }
            Desc -> compareByDescending { it.rank }
        }

        Added -> when (sorting.order) {
            Asc -> compareBy<PersonalListItem> { it.listedAt }
            Desc -> compareByDescending { it.listedAt }
        }.thenBy { it.rank }

        Runtime -> when (sorting.order) {
            Asc -> compareBy { it.totalRuntime }
            Desc -> compareByDescending { it.totalRuntime }
        }

        Released -> when (sorting.order) {
            Asc -> compareBy { it.released }
            Desc -> compareByDescending { it.released }
        }

        Rating -> when (sorting.order) {
            Asc -> compareBy { it.rating }
            Desc -> compareByDescending { it.rating }
        }

        // User ratings are not loaded here, so shows and movies each keep the order of the response.
        UserRating -> Comparator { _, _ -> 0 }

        Title -> when (sorting.order) {
            Asc -> compareBy { it.titleNormalized }
            Desc -> compareByDescending { it.titleNormalized }
        }
    }
}

private val PersonalListItem.totalRuntime: Duration?
    get() = show?.totalRuntime ?: movie?.runtime

private val PersonalListItem.released: Instant?
    get() = show?.releasedAt ?: movie?.released?.atStartOfDay(ZoneOffset.UTC)?.toInstant()

private val PersonalListItem.rating: Float?
    get() = (show?.rating ?: movie?.rating)?.rating

private val PersonalListItem.titleNormalized: String?
    get() = show?.titleNormalized ?: movie?.titleNormalized
