package tv.trakt.trakt.common.core.klipy.data.remote

import tv.trakt.trakt.common.core.klipy.model.GIFS_MIN_PER_PAGE
import tv.trakt.trakt.common.core.klipy.model.GifPage
import tv.trakt.trakt.common.core.klipy.model.GifsQuery
import tv.trakt.trakt.common.networking.api.klipy.KlipyApi

class GifsApiClient(
    private val api: KlipyApi,
) : GifsRemoteDataSource {
    override suspend fun getTrendingGifs(query: GifsQuery): GifPage {
        return api.getTrendingGifs(query.toRequest()).toDomain()
    }

    override suspend fun searchGifs(query: GifsQuery): GifPage {
        val term = query.term?.trim()
        if (term.isNullOrEmpty()) return GifPage.Empty

        val request = query
            .copy(
                term = term,
                pagination = query.pagination.copy(
                    limit = query.pagination.limit.coerceAtLeast(GIFS_MIN_PER_PAGE),
                ),
            )
            .toRequest()

        return api.searchGifs(request).toDomain()
    }
}
