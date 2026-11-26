package tv.trakt.trakt.core.mixtape.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.MixtapeRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.mixtape.MixtapeItem

internal class GetMixtapeHistoryUseCase(
    private val remoteSource: MixtapeRemoteSource,
) {
    suspend fun getHistory(
        type: MediaType,
        query: String? = null,
    ): ImmutableList<MixtapeItem> {
        return remoteSource.getHistory(type = type, query = query)
            .mapNotNull(MixtapeItem::fromDto)
            .distinctBy { it.id }
            .toImmutableList()
    }
}
