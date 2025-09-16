package tv.trakt.trakt.core.shows.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsLocalDataSource
import java.time.Instant

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal class GetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localRecommendedSource: RecommendedShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<Show> {
        return localRecommendedSource.getShows()
            .toImmutableList()
    }

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        skipLocal: Boolean = false,
    ): ImmutableList<Show> {
        return remoteSource.getRecommended(limit)
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also { shows ->
                if (skipLocal) return@also
                localRecommendedSource.addShows(
                    shows = shows.take(DEFAULT_LIMIT),
                    addedAt = Instant.now(),
                )
            }
    }
}
