package tv.trakt.trakt.core.ratings.allratings.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetSeasonRatingsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getSeasonRatings(showId: TraktId): ImmutableList<Season> {
        return remoteSource.getSeasons(showId)
            .map { Season.fromDto(it) }
            .filter { !it.isSpecial && it.rating.rating > 0 && (it.episodeCount ?: 0) > 0 }
            .sortedBy { it.number }
            .toImmutableList()
    }
}
