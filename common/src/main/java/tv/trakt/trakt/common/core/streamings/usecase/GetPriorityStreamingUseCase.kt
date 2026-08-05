package tv.trakt.trakt.common.core.streamings.usecase

import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.local.cacheSourcesIfNeeded
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.core.streamings.helpers.toStreamingService
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.networking.StreamingDto

/**
 * The single streaming service to offer the user first (watch-now button, context sheets),
 * picked from the subscriptions available in their own country.
 */
class GetPriorityStreamingUseCase(
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
    private val priorityStreamingProvider: PriorityStreamingServiceProvider,
) {
    suspend fun getStreamingService(
        user: User,
        request: StreamingsRequest,
    ): Result {
        localStreamingSource.cacheSourcesIfNeeded(remoteStreamingSource)

        val userCountry = user.streamings?.country ?: DEFAULT_COUNTRY_CODE

        // Every country is fetched so [hasNoOffering] can look beyond the user's own.
        val streamings = remoteStreamingSource.getStreamings(
            request.copy(countryCode = null),
        )

        val subscriptions = streamings[userCountry]?.subscription.orEmpty()
        val services = subscriptions.asyncMap {
            it.toStreamingService(
                country = userCountry,
                source = localStreamingSource.getStreamingSource(it.source),
            )
        }

        val priorityService = priorityStreamingProvider.findPriorityStreamingService(
            favoriteServices = user.streamings?.favorites.orEmpty(),
            streamingServices = services,
        )

        return Result(
            streamingService = priorityService,
            noServices = streamings.hasNoOffering(userCountry),
        )
    }

    /**
     * `true` only when the media is not offered anywhere: no free or subscription option in
     * any country, and nothing to buy in the user's own country.
     */
    private fun Map<String, StreamingDto>.hasNoOffering(userCountry: String): Boolean {
        return values.flatMap { it.free }.isEmpty() &&
            values.flatMap { it.subscription }.isEmpty() &&
            this[userCountry]?.purchase.isNullOrEmpty()
    }

    data class Result(
        val streamingService: StreamingService?,
        val noServices: Boolean,
    )
}
