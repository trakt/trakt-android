package tv.trakt.trakt.core.movies.sections.anticipated.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.movies.model.WatchersMovie

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal interface GetAnticipatedMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<WatchersMovie>

    suspend fun getMovies(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchersMovie>
}
