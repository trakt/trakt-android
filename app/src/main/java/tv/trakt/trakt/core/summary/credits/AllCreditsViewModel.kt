package tv.trakt.trakt.core.summary.credits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.usecases.spoilers.GetSpoilerStateUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.credits.model.CreditsMode
import tv.trakt.trakt.core.summary.credits.model.MediaCredits
import tv.trakt.trakt.core.summary.credits.navigation.AllCreditsDestination
import tv.trakt.trakt.core.summary.credits.usecases.GetMediaCreditsUseCase

@Suppress("UNCHECKED_CAST")
internal class AllCreditsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCreditsUseCase: GetMediaCreditsUseCase,
    private val getSpoilerStateUseCase: GetSpoilerStateUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllCreditsDestination>()

    private val initialState = AllCreditsState(
        mediaTitle = destination.mediaTitle,
        backgroundUrl = destination.backgroundUrl,
    )

    private val creditsState = MutableStateFlow(initialState.credits)
    private val modeState = MutableStateFlow(initialState.mode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val spoilersHiddenState = MutableStateFlow(initialState.spoilersHidden)

    init {
        loadData()
        observeSpoilers()
    }

    private fun observeSpoilers() {
        if (destination.mediaType != MediaType.Show) {
            return
        }

        getSpoilerStateUseCase.observeShowHidden(destination.mediaId.toTraktId())
            .onEach { hidden -> spoilersHiddenState.update { hidden } }
            .catch { error -> Timber.recordError(error) }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val spoilersHidden = async { loadSpoilersHidden() }
                val credits = getCreditsUseCase.getCredits(destination.toSource())

                spoilersHiddenState.update { spoilersHidden.await() }
                creditsState.update { credits }
                modeState.update { mode ->
                    if (mode in credits.modes) mode else credits.modes.firstOrNull() ?: mode
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadSpoilersHidden(): Boolean {
        if (destination.mediaType != MediaType.Show) {
            return false
        }

        return try {
            getSpoilerStateUseCase.isShowHidden(destination.mediaId.toTraktId())
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            // Unknown watched progress must not expose episode counts.
            true
        }
    }

    fun setMode(mode: CreditsMode) {
        modeState.update { mode }
    }

    fun logEpisodeCountReveal() {
        analytics.logSpoilerReveal(MediaType.Show.value)
    }

    val state = combine(
        creditsState,
        modeState,
        loadingState,
        errorState,
        spoilersHiddenState,
    ) { state ->
        AllCreditsState(
            mediaTitle = initialState.mediaTitle,
            backgroundUrl = initialState.backgroundUrl,
            credits = state[0] as MediaCredits?,
            mode = state[1] as CreditsMode,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
            spoilersHidden = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
