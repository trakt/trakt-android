package tv.trakt.trakt.core.profile.sections.library.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.core.profile.sections.library.model.LibraryFilter

internal class GetCollectionLimitServersUseCase(
    private val remoteSource: UserRemoteSource,
) {
    /**
     * Returns the names of the Plex servers that hit the free collection limit.
     * A null entry is a server without a name.
     */
    suspend fun getServers(): ImmutableList<String?> {
        return remoteSource.getSyncConnections()
            .filter { it.provider == LibraryFilter.DIGITAL.value && it.collectionLimitReached }
            .map { it.serverName }
            .toImmutableList()
    }
}
