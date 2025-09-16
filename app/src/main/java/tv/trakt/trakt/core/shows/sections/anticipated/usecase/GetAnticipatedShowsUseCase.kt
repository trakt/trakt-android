package tv.trakt.trakt.core.shows.sections.anticipated.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsLocalDataSource
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal class GetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localAnticipatedSource: AnticipatedShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<WatchersShow> {
        return localAnticipatedSource.getShows()
            .sortedByDescending { it.watchers }
            .toImmutableList()
    }

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchersShow> {
        return remoteSource.getAnticipated(
            page = page,
            limit = limit,
            endDate = nowUtcInstant().plus(365, DAYS).truncatedTo(DAYS),
        )
            .asyncMap {
                WatchersShow(
                    watchers = it.listCount,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                if (skipLocal) return@also
                localAnticipatedSource.addShows(
                    shows = shows.take(DEFAULT_LIMIT),
                    addedAt = Instant.now(),
                )
            }
    }
}
