package tv.trakt.trakt.core.klipy

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.core.klipy.model.Gif
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class GifPickerState(
    val query: String = "",
    val gifs: ImmutableList<Gif> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
) {
    /** True once a load finished without results - trending and search share the empty copy. */
    val isEmpty: Boolean
        get() = gifs.isEmpty() && loading.isDone && error == null
}
