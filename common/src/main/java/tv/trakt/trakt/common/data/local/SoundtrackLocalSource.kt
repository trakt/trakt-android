package tv.trakt.trakt.common.data.local

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack

class SoundtrackLocalSource {
    private val mutex = Mutex()
    private val cache = mutableMapOf<Pair<MediaType, TraktId>, ImmutableList<SoundtrackTrack>>()

    suspend fun getSoundtrack(
        type: MediaType,
        mediaId: TraktId,
    ): ImmutableList<SoundtrackTrack>? {
        return mutex.withLock { cache[type to mediaId] }
    }

    suspend fun setSoundtrack(
        type: MediaType,
        mediaId: TraktId,
        tracks: ImmutableList<SoundtrackTrack>,
    ) {
        mutex.withLock { cache[type to mediaId] = tracks }
    }

    suspend fun clear() {
        mutex.withLock { cache.clear() }
    }
}
