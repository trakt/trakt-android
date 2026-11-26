package tv.trakt.trakt.core.mixtape.usecases

import tv.trakt.trakt.common.data.remote.MixtapeRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.MixtapeVectors

internal class GetMixtapeVectorsUseCase(
    private val remoteSource: MixtapeRemoteSource,
) {
    suspend fun getVectors(
        type: MediaType,
        ids: Collection<TraktId>,
    ): MixtapeVectors {
        if (ids.isEmpty()) return MixtapeVectors()

        val sortedIds = ids.distinct().sortedBy { it.value }
        val dto = remoteSource.getVectors(type = type, ids = sortedIds)
            ?: return MixtapeVectors()

        return MixtapeVectors.fromDto(dto)
    }
}
