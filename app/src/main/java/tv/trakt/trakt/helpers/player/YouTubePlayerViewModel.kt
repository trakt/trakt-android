package tv.trakt.trakt.helpers.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tv.trakt.trakt.helpers.player.navigation.YouTubePlayerDestination

internal class YouTubePlayerViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<YouTubePlayerDestination>()

    private val videoUrlState = MutableStateFlow(destination.videoUrl)
    private val videoIdState = MutableStateFlow(destination.videoUrl.substringAfterLast("v="))

    val state = combine(
        videoUrlState,
        videoIdState,
    ) { url, id ->
        YouTubePlayerState(
            videoUrl = url,
            videoId = id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = YouTubePlayerState(),
    )
}
