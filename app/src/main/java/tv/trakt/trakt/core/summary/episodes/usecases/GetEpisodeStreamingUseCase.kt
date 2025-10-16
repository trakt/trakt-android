package tv.trakt.trakt.core.summary.episodes.usecases

import android.icu.util.Currency
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.model.streamings.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetEpisodeStreamingUseCase(
    private val remoteEpisodeSource: EpisodesRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
    private val priorityStreamingProvider: PriorityStreamingServiceProvider,
) {
    suspend fun getStreamingService(
        user: User,
        show: Show,
        episode: Episode,
    ): Result {
        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val userCountry = user.streamings?.country ?: DEFAULT_COUNTRY_CODE
        val streamings = remoteEpisodeSource.getStreamings(
            showId = show.ids.trakt,
            season = episode.season,
            episode = episode.number,
            countryCode = null,
        )

        val subscriptions = streamings[userCountry]?.subscription ?: emptyList()

        val result = subscriptions
            .asyncMap {
                val localSource = localStreamingSource.getStreamingSource(it.source)
                StreamingService(
                    name = localSource?.name ?: "",
                    linkDirect = it.linkDirect,
                    linkAndroid = it.linkAndroid,
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

        return Result(
            streamingService = priorityService,
            noServices = priorityService == null,
        )
    }

    data class Result(
        val streamingService: StreamingService?,
        val noServices: Boolean,
    )
}
