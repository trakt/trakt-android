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
    private val destination = savedStateHandle.toRoute<YouTubePlayerDestination>()

    private val videoUrlState = MutableStateFlow(destination.videoUrl)
    private val videoIdState = MutableStateFlow(destination.videoUrl.substringAfterLast("v="))

    val state = combine(
        videoUrlState,
        videoIdState,
    ) { state ->
        YouTubePlayerState(
            videoUrl = state[0] as String?,
            videoId = state[1] as String?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = YouTubePlayerState(),
    )
}
