package tv.trakt.trakt.common.core.streamings.data.remote

import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.StreamingSourceDto

interface StreamingRemoteDataSource {
    suspend fun getStreamingSources(): List<StreamingSourceDto>

    /**
     * Watchnow offers for the requested media, keyed by country code.
     */
    suspend fun getStreamings(request: StreamingsRequest): Map<String, StreamingDto>

    /**
     * JustWatch page for the requested media in [StreamingsRequest.countryCode], or `null`
     * when that country has no page. An all-countries request has no single link to
     * return, so it always yields `null`.
     */
    suspend fun getJustWatchLink(request: StreamingsRequest): String?
}
