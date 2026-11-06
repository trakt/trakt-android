package tv.trakt.trakt.core.summary.soundtrack.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.soundtrack.all.navigation.AllSoundtrackDestination
import tv.trakt.trakt.core.summary.soundtrack.usecases.GetSoundtrackUseCase

private const val PLAY_SOURCE = "soundtrack-screen"
private const val UNKNOWN_MATCH = "unknown"

@Suppress("UNCHECKED_CAST")
internal class AllSoundtrackViewModel(
    savedStateHandle: SavedStateHandle,
    private val getSoundtrackUseCase: GetSoundtrackUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllSoundtrackDestination>()

    private val initialState = AllSoundtrackState(
        mediaTitle = destination.mediaTitle,
        posterUrl = destination.posterUrl,
        backgroundUrl = destination.backgroundUrl,
        currentSeason = destination.currentSeason,
    )

    private val tracksState = MutableStateFlow(initialState.tracks)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()

        analytics.logScreenView(
            screenName = "all_soundtrack",
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                tracksState.update {
                    getSoundtrackUseCase.getSoundtrack(
                        type = destination.mediaType,
                        mediaId = destination.mediaId.toTraktId(),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    tracksState.update { persistentListOf() }
                    if (error.getHttpCode() != HTTP_ERROR_NOT_FOUND) {
                        errorState.update { error }
                        Timber.recordError(error)
                    }
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun logPlay(track: SoundtrackTrack) {
        analytics.playback.logSoundtrack(
            source = PLAY_SOURCE,
            position = track.position,
            matchedOn = track.matchedOn ?: UNKNOWN_MATCH,
        )
    }

    val state = combine(
        tracksState,
        loadingState,
        errorState,
    ) { state ->
        initialState.copy(
            tracks = state[0] as ImmutableList<SoundtrackTrack>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
