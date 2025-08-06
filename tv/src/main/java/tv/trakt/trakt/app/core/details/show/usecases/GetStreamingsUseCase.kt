package tv.trakt.trakt.app.core.details.show.usecases

import android.icu.util.Currency
import tv.trakt.trakt.app.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.common.model.TraktId
import tv.trakt.trakt.app.common.model.User
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import tv.trakt.trakt.app.core.streamings.model.fromDto
import tv.trakt.trakt.app.core.streamings.utilities.PriorityStreamingServiceProvider
import tv.trakt.trakt.app.helpers.extensions.asyncMap
import tv.trakt.trakt.app.networking.openapi.StreamingDto
import kotlin.collections.flatMap

internal class GetStreamingsUseCase(
    private val remoteShowSource: ShowsRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
    private val priorityStreamingProvider: PriorityStreamingServiceProvider,
) {
    suspend fun getStreamingService(
        user: User,
        showId: TraktId,
    ): Result {

        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val countryCode = user.streamings?.country ?: DEFAULT_COUNTRY_CODE
        val streamings: Map<String, StreamingDto> = remoteShowSource.getShowStreamings(
            showId = showId,
            countryCode = null
        )

        val subscriptions = streamings[countryCode]?.subscription ?: emptyList()

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
                    country = countryCode,
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

        val noService = streamings.values.run {
            flatMap { it.subscription }.isEmpty() &&
                flatMap { it.purchase }.isEmpty() &&
                flatMap { it.free }.isEmpty()
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
