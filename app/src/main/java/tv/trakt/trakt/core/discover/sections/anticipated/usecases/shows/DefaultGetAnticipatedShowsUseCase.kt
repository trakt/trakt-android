package tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

internal class DefaultGetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localAnticipatedSource: AnticipatedShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
) : GetAnticipatedShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localAnticipatedSource.getShows()
            .sortedByDescending { it.count }
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
    ): ImmutableList<DiscoverItem.ShowItem> {
        return remoteSource.getAnticipated(
            page = page,
            limit = limit,
            endDate = nowUtcInstant().plus(365, DAYS).truncatedTo(DAYS),
        )
            .asyncMap {
                DiscoverItem.ShowItem(
                    show = Show.fromDto(it.show),
                    count = it.listCount,
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localAnticipatedSource.addShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
