package tv.trakt.trakt.core.shows.sections.recommended.usecase.recommended

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.recommended.usecase.DEFAULT_LIMIT
import tv.trakt.trakt.core.shows.sections.recommended.usecase.GetRecommendedShowsUseCase
import java.time.Instant

internal class HalloweenGetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localRecommendedSource: RecommendedShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
) : GetRecommendedShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<Show> {
        return localRecommendedSource.getShows()
            .toImmutableList()
            .also {
                localShowSource.upsertShows(it)
            }
    }

    override suspend fun getShows(
        limit: Int,
        skipLocal: Boolean,
    ): ImmutableList<Show> {
        return remoteSource.getRecommended(
            limit = limit,
            genres = listOf("horror"),
            subgenres = listOf(
                "halloween",
                "haunting",
                "nightmare",
                "witch",
                "monster",
                "demon",
                "occult",
                "supernatural",
            ),
        )
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localRecommendedSource.addShows(
                        shows = shows.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(shows)
            }
    }
}
