package tv.trakt.trakt.core.summary.sentiment.usecases

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.data.remote.RealityCheckRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.realitycheck.DocumentaryCheck
import java.util.Locale

internal class GetDocumentaryCheckUseCase(
    private val remoteSource: RealityCheckRemoteSource,
) {
    suspend fun getDocumentaryCheck(
        mediaId: TraktId,
        mediaType: MediaType,
    ): DocumentaryCheck? {
        return remoteSource
            .getDocumentaryCheck(
                type = mediaType,
                mediaId = mediaId,
                locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
            )
            ?.let(DocumentaryCheck::fromDto)
    }
}
