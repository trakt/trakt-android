package tv.trakt.trakt.common.core.klipy.data.remote

import tv.trakt.trakt.common.core.klipy.model.GifPage
import tv.trakt.trakt.common.core.klipy.model.GifsQuery

interface GifsRemoteDataSource {
    suspend fun getTrendingGifs(query: GifsQuery = GifsQuery()): GifPage

    /** Blank terms short-circuit to [GifPage.Empty] - KLIPY rejects an empty `q`. */
    suspend fun searchGifs(query: GifsQuery): GifPage
}
