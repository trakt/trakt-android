package tv.trakt.trakt.core.shows.sections.anticipated.usecases.anticipated

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.DEFAULT_LIMIT
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import java.time.Instant

internal class HalloweenGetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localAnticipatedSource: AnticipatedShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
) : GetAnticipatedShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<WatchersShow> {
        return localAnticipatedSource.getShows()
            .sortedByDescending { it.watchers }
            .toImmutableList()
            .also {
                localShowSource.upsertShows(
                    it.asyncMap { item -> item.show },
                )
            }
    }

    override suspend fun getShows(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
    ): ImmutableList<WatchersShow> {
        val now = nowLocal()
        return remoteSource.getAnticipated(
            page = page,
            limit = limit,
            years = "${now.minusYears(3).year}-${now.year}",
            subgenres = listOf("halloween"),
        )
            .asyncMap {
                WatchersShow(
                    watchers = it.listCount,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localAnticipatedSource.addShows(
                        shows = shows.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
