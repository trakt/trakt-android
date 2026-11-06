package tv.trakt.trakt.core.summary.soundtrack

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack

@Immutable
internal data class SoundtrackState(
    val user: User? = null,
    val tracks: ImmutableList<SoundtrackTrack>? = null,
    val collapsed: Boolean = false,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
) {
    val isVip: Boolean
        get() = user?.isAnyVip == true
}
