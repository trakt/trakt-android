package tv.trakt.trakt.core.summary.sentiment.usecases

import io.ktor.client.plugins.ClientRequestException
import tv.trakt.trakt.common.data.remote.MediaRemoteSource
import tv.trakt.trakt.common.model.MediaMatch
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

internal class GetMediaMatchUseCase(
    private val remoteSource: MediaRemoteSource,
) {
    suspend fun getMatch(
        mediaId: TraktId,
        mediaType: MediaType,
    ): MediaMatch? {
        return try {
            MediaMatch.fromDto(
                remoteSource.getMatch(
                    type = mediaType,
                    mediaId = mediaId,
                ),
            )
        } catch (_: ClientRequestException) {
            // The match route answers only some accounts and refuses the rest with a 4xx.
            null
        }
    }
}
