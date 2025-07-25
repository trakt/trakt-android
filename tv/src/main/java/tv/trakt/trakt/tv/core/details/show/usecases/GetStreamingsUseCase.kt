package tv.trakt.trakt.tv.core.details.show.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.tv.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.tv.common.model.StreamingService
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.tv.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.tv.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.tv.core.streamings.model.StreamingSource
import tv.trakt.trakt.tv.core.streamings.model.fromDto
import tv.trakt.trakt.tv.core.streamings.utilities.PriorityStreamingServiceProvider
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

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
                    uhd = it.uhd,
                )
            }

        return priorityStreamingProvider.findPriorityStreamingService(
            favoriteServices = user.streamings?.favorites ?: emptyList(),
            streamingServices = result,
        )
    }
}
