package tv.trakt.trakt.core.mixtape.usecases

import tv.trakt.trakt.common.data.remote.MixtapeRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape

internal class GetMixtapeUseCase(
    private val remoteSource: MixtapeRemoteSource,
) {
    suspend fun getMixtape(
        type: MediaType,
        picks: List<TraktId>,
        suggestions: Boolean,
    ): Mixtape {
        if (picks.isEmpty()) return Mixtape()

        val dto = remoteSource.getMixtape(
            type = type,
            picks = picks,
            suggestions = suggestions,
        )
        return Mixtape.fromDto(dto)
    }
}
