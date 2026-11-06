package tv.trakt.trakt.core.summary.soundtrack.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack

@Stable
internal class SoundtrackPlayback {
    var selected by mutableStateOf<SoundtrackTrack?>(null)
        private set

    /** Bumped on every play request, so tapping the same row again toggles playback. */
    var playToken by mutableIntStateOf(0)
        private set

    var playing by mutableStateOf(false)
        private set

    /** Set from a play request until the embed reports playback started. */
    private var loading by mutableStateOf(false)

    fun play(track: SoundtrackTrack) {
        val pausing = playing && isSelected(track)
        selected = track
        loading = !pausing
        playToken += 1
    }

    fun stop() {
        selected = null
        playing = false
        loading = false
    }

    fun onPlaybackUpdate(playing: Boolean) {
        this.playing = playing
        if (playing) loading = false
    }

    fun isPlaying(track: SoundtrackTrack): Boolean {
        return playing && selected?.position == track.position
    }

    fun isLoading(track: SoundtrackTrack): Boolean {
        return loading && isSelected(track)
    }

    fun isSelected(track: SoundtrackTrack): Boolean {
        return selected?.position == track.position
    }
}

@Composable
internal fun rememberSoundtrackPlayback(): SoundtrackPlayback {
    return remember { SoundtrackPlayback() }
}
