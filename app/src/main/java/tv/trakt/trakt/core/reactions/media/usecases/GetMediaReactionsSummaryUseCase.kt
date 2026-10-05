package tv.trakt.trakt.core.reactions.media.usecases

import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.core.reactions.media.data.remote.MediaReactionsRemoteDataSource

internal class GetMediaReactionsSummaryUseCase(
    private val remoteSource: MediaReactionsRemoteDataSource,
) {
    suspend fun getSummary(target: MediaReactionsTarget): MediaReactionsSummary {
        return MediaReactionsSummary.fromDto(remoteSource.getSummary(target))
    }
}
