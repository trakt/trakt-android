package tv.trakt.app.tv.core.streamings.utilities

import androidx.compose.ui.util.fastAny
import tv.trakt.app.tv.common.model.StreamingService

/**
 * A utility class responsible for determining the priority streaming service for a given user
 * among a list of available streaming services.
 */
internal class PriorityStreamingServiceProvider {
    /**
     * Finds the priority streaming service from a list of available services based on user favorites.
     *
     * The priority is determined based on the following criteria, in order:
     * 1. Services that have a direct link.
     * 2. Services that are present in the `favoriteServices` list.
     * 3. Services that offer UHD quality.
     *
     * @param favoriteServices A list of the user's favorite streaming services.
     * @param streamingServices A list of available streaming services to choose from.
     * @return The highest priority [StreamingService] based on the criteria,
     *         or `null` if no suitable service is found or the input `streamingServices` list is empty.
     */
    fun findPriorityStreamingService(
        favoriteServices: List<String>,
        streamingServices: List<StreamingService>,
    ): StreamingService? {
        return streamingServices
            .filter { it.linkDirect != null }
            .sortedWith(
                compareByDescending<StreamingService> { service ->
                    favoriteServices.fastAny { favorite ->
                        favorite.contains(
                            service.source,
                            ignoreCase = true,
                        )
                    }
                }.thenByDescending {
                    it.uhd
                },
            )
            .firstOrNull()
    }
}
