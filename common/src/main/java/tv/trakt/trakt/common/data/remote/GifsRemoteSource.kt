package tv.trakt.trakt.common.data.remote

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.klipy.data.remote.toDomain
import tv.trakt.trakt.common.core.klipy.data.remote.toRequest
import tv.trakt.trakt.common.core.klipy.model.GIFS_MIN_PER_PAGE
import tv.trakt.trakt.common.core.klipy.model.GifCategory
import tv.trakt.trakt.common.core.klipy.model.GifPage
import tv.trakt.trakt.common.core.klipy.model.GifsQuery
import tv.trakt.trakt.common.networking.api.klipy.KlipyApi

class GifsRemoteSource(
    private val api: KlipyApi,
) {
    suspend fun getTrendingGifs(query: GifsQuery = GifsQuery()): GifPage {
        return api.getTrendingGifs(query.toRequest()).toDomain()
    }

    suspend fun getCategories(customerId: String?): ImmutableList<GifCategory> {
        return api.getCategories(customerId)
            .categories
            .map(GifCategory::fromDto)
            .toImmutableList()
    }

    /** Blank terms short-circuit to [GifPage.Empty] - KLIPY rejects an empty `q`. */
    suspend fun searchGifs(query: GifsQuery): GifPage {
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
