package tv.trakt.trakt.core.reactions.media.usecases

import tv.trakt.trakt.common.core.reactions.media.data.remote.MediaReactionsRemoteSource
import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget

internal class GetMediaReactionsSummaryUseCase(
    private val remoteSource: MediaReactionsRemoteSource,
) {
    suspend fun getSummary(target: MediaReactionsTarget): MediaReactionsSummary {
        return MediaReactionsSummary.fromDto(remoteSource.getSummary(target))
    }
}
