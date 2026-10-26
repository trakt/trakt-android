package tv.trakt.trakt.core.home.sections.upnext.model

import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.core.home.model.UpNextMovie
import tv.trakt.trakt.common.core.home.model.UpNextShow
import tv.trakt.trakt.common.model.sorting.SortOrder
import java.time.ZoneOffset

/**
 * Merges movies into shows that already arrive sorted from the API, keeping the shows' order
 * and placing each movie before the first show it sorts ahead of.
 */
internal fun interleaveUpNext(
    shows: List<UpNextShow>,
    movies: List<UpNextMovie>,
    sorting: UpNextSorting,
): List<UpNextItem> {
    val direction = if (sorting.order == SortOrder.Asc) 1 else -1
    val sortedMovies = movies.sortedWith { a, b ->
        a.sortValue(sorting.type).compareTo(b.sortValue(sorting.type)) * direction
    }

    val (merged, inserted) = shows.fold(emptyList<UpNextItem>() to emptySet<String>()) { (items, keys), show ->
        val showValue = show.sortValue(sorting.type)
        val before = sortedMovies.filter { movie ->
            movie.key !in keys && movie.sortValue(sorting.type).compareTo(showValue) * direction < 0
        }
        (items + before + show) to (keys + before.map { it.key })
    }

    return merged + sortedMovies.filter { it.key !in inserted }
}

private fun UpNextShow.sortValue(type: UpNextSortType): Long {
    return when (type) {
        UpNextSortType.Released -> {
            progress.nextEpisode?.releasedAt?.toEpochMilli() ?: 0
        }
        UpNextSortType.Remaining -> {
            (progress.aired - progress.completed).toLong()
        }
        UpNextSortType.Smart, UpNextSortType.Default -> {
            progress.lastWatchedAt?.toInstant()?.toEpochMilli() ?: 0
        }
    }
}

private fun UpNextMovie.sortValue(type: UpNextSortType): Long {
    return when (type) {
        UpNextSortType.Released -> {
            movie.released?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: 0
        }
        UpNextSortType.Remaining -> {
            0
        }
        UpNextSortType.Smart, UpNextSortType.Default -> {
            progress.pausedAt.toEpochMilli()
        }
    }
}
