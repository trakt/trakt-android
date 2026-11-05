package tv.trakt.trakt.core.summary.soundtrack.usecases

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.common.networking.api.v3.V3Api
import java.util.Locale

internal class GetSoundtrackUseCase(
    private val v3Api: V3Api,
) {
    suspend fun getSoundtrack(
        type: MediaType,
        mediaId: TraktId,
    ): ImmutableList<SoundtrackTrack> {
        return v3Api
            .getSoundtrack(
                type = type,
                mediaId = mediaId,
                locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.US,
            )
            .map(SoundtrackTrack::fromDto)
            .sortedBy { it.position }
            .toImmutableList()
    }
}
