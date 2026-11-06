package tv.trakt.trakt.core.summary.soundtrack.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack

@Immutable
internal data class AllSoundtrackState(
    val mediaTitle: String = "",
    val posterUrl: String? = null,
    val backgroundUrl: String? = null,
    val currentSeason: Int? = null,
    val tracks: ImmutableList<SoundtrackTrack>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
