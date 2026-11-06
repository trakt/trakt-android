package tv.trakt.trakt.core.summary.soundtrack.usecases

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.SoundtrackLocalSource
import tv.trakt.trakt.common.data.remote.SoundtrackRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import java.util.Locale

internal class GetSoundtrackUseCase(
    private val remoteSource: SoundtrackRemoteSource,
    private val localSource: SoundtrackLocalSource,
) {
    suspend fun getSoundtrack(
        type: MediaType,
        mediaId: TraktId,
    ): ImmutableList<SoundtrackTrack> {
        localSource.getSoundtrack(type, mediaId)?.let { return it }

        val tracks = remoteSource
            .getSoundtrack(
                type = type,
                mediaId = mediaId,
                locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
            )
            .map(SoundtrackTrack::fromDto)
            .sortedBy { it.position }
            .toImmutableList()

        localSource.setSoundtrack(type, mediaId, tracks)
        return tracks
    }
}
