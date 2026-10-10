package tv.trakt.trakt.common.core.media.data.remote

import org.openapitools.client.apis.MediaApi
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.AnticipatedMediaDto
import tv.trakt.trakt.common.networking.PopularMediaDto
import tv.trakt.trakt.common.networking.TrendingMediaDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationsRequest

class MediaApiClient(
    private val mediaApi: MediaApi,
    private val v3Api: V3Api,
) : MediaRemoteDataSource {
    override suspend fun getTrending(
        page: Int,
        limit: Int,
        filters: GlobalFilter,
    ): List<TrendingMediaDto> {
        val response = mediaApi.getMediaTrending(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getPopular(
        page: Int,
        limit: Int,
        filters: GlobalFilter,
    ): List<PopularMediaDto> {
        val response = mediaApi.getMediaPopular(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(
        page: Int,
        limit: Int,
        filters: GlobalFilter,
    ): List<AnticipatedMediaDto> {
        val response = mediaApi.getMediaAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter,
    ): List<V3MediaRecommendationResponse> {
        return v3Api.getMediaRecommendations(
            V3RecommendationsRequest(
                limit = limit,
                extended = "full,streaming_ids,cloud9,colors",
                watchWindow = 25,
                watchnow = filters.availability?.joinToString(",") { it.slug },
                genres = filters.genre?.joinToString(",") { it.slug },
                subgenres = filters.subgenre?.joinToString(","),
                years = filters.years?.let { "${it.first}-${it.second}" },
                ratings = filters.rating?.let { "${it.first}-${it.second}" },
                runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
                certifications = filters.certification?.joinToString(",") { it.slug },
                countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
                statuses = filters.statuses?.joinToString(",") { it.slug },
                ignoreWatched = true,
                ignoreWatchlisted = filters.hideWatchlist,
                ignoreCollected = true,
            ),
        )
    }
}
