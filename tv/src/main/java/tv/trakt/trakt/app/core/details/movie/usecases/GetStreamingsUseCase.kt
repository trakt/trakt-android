package tv.trakt.trakt.app.core.details.movie.usecases

import android.icu.util.Currency
import tv.trakt.trakt.app.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import tv.trakt.trakt.app.core.streamings.model.fromDto
import tv.trakt.trakt.app.core.streamings.utilities.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.StreamingDto

internal class GetStreamingsUseCase(
    private val remoteMovieSource: MoviesRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
    private val priorityStreamingProvider: PriorityStreamingServiceProvider,
) {
    suspend fun getStreamingService(
        user: User,
        movieId: TraktId,
    ): Result {
        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val userCountry = user.streamings?.country ?: DEFAULT_COUNTRY_CODE
        val streamings: Map<String, StreamingDto> = remoteMovieSource.getMovieStreamings(
            movieId = movieId,
            countryCode = null,
        )

        val subscriptions = streamings[userCountry]?.subscription ?: emptyList()

        val result = subscriptions
            .asyncMap {
                val localSource = localStreamingSource.getStreamingSource(it.source)
                StreamingService(
                    name = localSource?.name ?: "",
                    linkDirect = it.linkDirect,
                    source = it.source,
                    color = localSource?.color,
                    logo = localSource?.images?.logo,
                    channel = localSource?.images?.channel,
                    uhd = it.uhd,
                    country = userCountry,
                    purchasePrice = it.prices.purchase,
                    rentPrice = it.prices.rent,
                    currency = it.currency?.let { code ->
                        Currency.getInstance(code)
                    },
                )
            }

        val priorityService = priorityStreamingProvider.findPriorityStreamingService(
            favoriteServices = user.streamings?.favorites ?: emptyList(),
            streamingServices = result,
        )

        val noService = streamings.run {
            values.flatMap { it.free }.isEmpty() &&
                values.flatMap { it.subscription }.isEmpty() &&
                filter { it.key == userCountry }.values.flatMap { it.purchase }.isEmpty()
        }

        return Result(
            streamingService = priorityService,
            noServices = noService,
        )
    }

    data class Result(
        val streamingService: StreamingService?,
        val noServices: Boolean,
    )
}
