package tv.trakt.trakt.core.userprofile.sections.match.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.UserMatch

internal const val MATCH_STRIP_LIMIT = 5

@Immutable
internal data class MatchStripEntry(
    val id: TraktId,
    val type: Type,
    val isFavorite: Boolean,
) {
    enum class Type {
        Movie,
        Show,
    }
}

internal val UserMatch.sharedCount: Int
    get() = sharedMovies.size + sharedShows.size

internal fun UserMatch.toMatchStrip(limit: Int = MATCH_STRIP_LIMIT): ImmutableList<MatchStripEntry> {
    val favoriteMovies = favoriteMovies.toSet()
    val favoriteShows = favoriteShows.toSet()

    val movies = sharedMovies.map {
        MatchStripEntry(id = it, type = MatchStripEntry.Type.Movie, isFavorite = it in favoriteMovies)
    }
    val shows = sharedShows.map {
        MatchStripEntry(id = it, type = MatchStripEntry.Type.Show, isFavorite = it in favoriteShows)
    }

    val interleaved = (0 until maxOf(movies.size, shows.size))
        .flatMap { listOfNotNull(movies.getOrNull(it), shows.getOrNull(it)) }

    return interleaved
        .sortedByDescending { it.isFavorite }
        .take(limit)
        .toImmutableList()
}
