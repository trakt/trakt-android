package tv.trakt.trakt.core.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SlugId

internal class ShareViewModel(
    mediaSlug: SlugId,
    mediaType: MediaType,
) : ViewModel() {
    private val initialState = ShareState(
        media = ShareState.Media(
            type = mediaType,
            slug = mediaSlug,
        ),
    )

    private val mediaState = MutableStateFlow(initialState.media)
    private val loadingState = MutableStateFlow(initialState.loading)

    val state = combine(
        mediaState,
        loadingState,
    ) { state ->
        ShareState(
            media = state[0] as? ShareState.Media,
            loading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
