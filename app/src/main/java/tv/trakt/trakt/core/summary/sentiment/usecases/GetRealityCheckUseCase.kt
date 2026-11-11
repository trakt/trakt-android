package tv.trakt.trakt.core.summary.sentiment.usecases

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.data.remote.RealityCheckRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import java.util.Locale

internal class GetRealityCheckUseCase(
    private val remoteSource: RealityCheckRemoteSource,
) {
    suspend fun getRealityCheck(
        mediaId: TraktId,
        mediaType: MediaType,
    ): RealityCheck? {
        return remoteSource
            .getRealityCheck(
                type = mediaType,
                mediaId = mediaId,
                locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
            )
            ?.let(RealityCheck::fromDto)
    }
}
