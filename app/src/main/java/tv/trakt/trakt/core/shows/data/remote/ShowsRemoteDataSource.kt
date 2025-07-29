package tv.trakt.trakt.core.shows.data.remote

import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto

internal interface ShowsRemoteDataSource {
    suspend fun getTrending(limit: Int): List<TrendingShowDto>
}
