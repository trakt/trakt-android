package tv.trakt.trakt.app.core.details.show.usecases

import android.icu.util.Currency
import tv.trakt.trakt.app.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import tv.trakt.trakt.app.core.streamings.model.fromDto
import tv.trakt.trakt.app.core.streamings.utilities.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

internal class GetStreamingsUseCase(
    private val remoteShowSource: ShowsRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
    private val priorityStreamingProvider: PriorityStreamingServiceProvider,
) {
    suspend fun getStreamingService(
        user: User,
        showId: TraktId,
    ): StreamingService? {
        val countryCode = user.streamings?.country ?: DEFAULT_COUNTRY_CODE

        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources(countryCode)
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val streamings = remoteShowSource.getShowStreamings(showId, countryCode)
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

        return priorityStreamingProvider.findPriorityStreamingService(
            favoriteServices = user.streamings?.favorites ?: emptyList(),
            streamingServices = result,
        )
    }
}
