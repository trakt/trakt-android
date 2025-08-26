package tv.trakt.trakt.core.sync.data.remote.shows

import tv.trakt.trakt.common.networking.ProgressShowDto

internal interface ShowsSyncRemoteDataSource {
    suspend fun getUpNext(
        limit: Int,
        page: Int,
    ): List<ProgressShowDto>
}
