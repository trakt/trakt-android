package tv.trakt.trakt.app.core.details.episode.usecases.streamings

import android.icu.util.Currency
import tv.trakt.trakt.app.Config
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import tv.trakt.trakt.app.core.streamings.model.fromDto
import tv.trakt.trakt.app.core.streamings.utilities.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

internal class GetStreamingsUseCase(
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
    private val priorityStreamingProvider: PriorityStreamingServiceProvider,
) {
    suspend fun getStreamingService(
        user: User,
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): Result {
        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.Companion.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val userCountry = user.streamings?.country ?: Config.DEFAULT_COUNTRY_CODE
        val streamings = remoteEpisodesSource.getEpisodeStreamings(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
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
