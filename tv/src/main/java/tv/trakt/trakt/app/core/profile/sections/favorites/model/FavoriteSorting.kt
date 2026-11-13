package tv.trakt.trakt.app.core.profile.sections.favorites.model

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

internal fun getFavoritesSorting(sorting: Sorting): Comparator<FavoriteItem> {
    return when (sorting.type) {
        Rank -> when (sorting.order) {
            Asc -> compareBy { it.rank }
            Desc -> compareByDescending { it.rank }
        }

        Added -> when (sorting.order) {
            Asc -> compareBy { it.listedAt }
            Desc -> compareByDescending { it.listedAt }
        }

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

private val FavoriteItem.rank: Int
    get() = when (this) {
        is FavoriteItem.ShowItem -> rank
        is FavoriteItem.MovieItem -> rank
    }

private val FavoriteItem.listedAt: Instant
    get() = when (this) {
        is FavoriteItem.ShowItem -> listedAt
        is FavoriteItem.MovieItem -> listedAt
    }

private val FavoriteItem.totalRuntime: Duration?
    get() = when (this) {
        is FavoriteItem.ShowItem -> show.totalRuntime
        is FavoriteItem.MovieItem -> movie.runtime
    }

private val FavoriteItem.released: Instant?
    get() = when (this) {
        is FavoriteItem.ShowItem -> show.releasedAt
        is FavoriteItem.MovieItem -> movie.released?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
    }

private val FavoriteItem.rating: Float?
    get() = when (this) {
        is FavoriteItem.ShowItem -> show.rating.rating
        is FavoriteItem.MovieItem -> movie.rating.rating
    }

private val FavoriteItem.titleNormalized: String
    get() = when (this) {
        is FavoriteItem.ShowItem -> show.titleNormalized
        is FavoriteItem.MovieItem -> movie.titleNormalized
    }
