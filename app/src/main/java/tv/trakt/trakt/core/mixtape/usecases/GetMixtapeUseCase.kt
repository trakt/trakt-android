package tv.trakt.trakt.core.mixtape.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.data.local.MixtapeLocalSource
import tv.trakt.trakt.common.data.remote.MixtapeRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion

internal class GetMixtapeUseCase(
    private val remoteSource: MixtapeRemoteSource,
    private val localSource: MixtapeLocalSource,
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

    suspend fun preloadSuggestions(
        type: MediaType,
        picks: List<TraktId>,
    ) {
        val mixtape = getMixtape(
            type = type,
            picks = picks,
            suggestions = true,
        )
        localSource.setSuggestions(type, picks, mixtape.suggestions)
    }

    fun takePreloadedSuggestions(
        type: MediaType,
        picks: List<TraktId>,
    ): ImmutableList<Suggestion>? {
        return localSource.takeSuggestions(type, picks)
    }
}
