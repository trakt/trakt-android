package tv.trakt.trakt.core.summary.shows.features.info.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowNetworksUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getNetworks(show: Show): ImmutableList<String> {
        val seasonNetworks = remoteSource.getSeasons(show.ids.trakt)
            .sortedBy { it.number }
            .mapNotNull { it.network }

        return (listOfNotNull(show.network) + seasonNetworks)
            .filter { it.isNotBlank() }
            .distinct()
            .toImmutableList()
    }
}
