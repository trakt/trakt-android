package tv.trakt.trakt.helpers.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tv.trakt.trakt.helpers.player.navigation.YouTubePlayerDestination

@OptIn(FlowPreview::class)
internal class YouTubePlayerViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val initialState = YouTubePlayerState()
    private val destination = savedStateHandle.toRoute<YouTubePlayerDestination>()

    private val videoState = MutableStateFlow(destination.videoUrl.substringAfterLast("v="))

    val state = combine(
        videoState,
    ) { state ->
        YouTubePlayerState(
            videoId = state[0] as String?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
