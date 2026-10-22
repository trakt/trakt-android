package tv.trakt.trakt.core.klipy

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.core.klipy.model.GifCategory
import tv.trakt.trakt.common.core.klipy.model.KlipyGif
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class GifPickerState(
    val query: String = "",
    val categories: ImmutableList<GifCategory> = persistentListOf(),
    val categoriesLoading: LoadingState = LoadingState.Idle,
    val gifs: ImmutableList<KlipyGif> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
) {
    /** True once a load finished without results - trending and search share the empty copy. */
    val isEmpty: Boolean
        get() = gifs.isEmpty() && loading.isDone && error == null

    val showCategories: Boolean
        get() = query.isBlank() && (categories.isNotEmpty() || !categoriesLoading.isDone)
}
