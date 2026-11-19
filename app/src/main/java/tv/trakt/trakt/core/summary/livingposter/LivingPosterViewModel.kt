package tv.trakt.trakt.core.summary.livingposter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.usecases.FeatureFlagsUseCase
import tv.trakt.trakt.core.summary.livingposter.usecases.GetLivingPosterUseCase

@Suppress("UNCHECKED_CAST")
internal class LivingPosterViewModel(
    private val mediaType: MediaType,
    private val mediaId: TraktId,
    private val getLivingPosterUseCase: GetLivingPosterUseCase,
    private val featureFlagsUseCase: FeatureFlagsUseCase,
) : ViewModel() {
    private val initialState = LivingPosterState()

    private val enabledState = MutableStateFlow(initialState.enabled)
    private val posterState = MutableStateFlow(initialState.poster)

    private var loadJob: Job? = null

    init {
        observeEnabled()
    }

    private fun observeEnabled() {
        featureFlagsUseCase.observeEnabled(FeatureFlag.LivingPoster)
            .onEach { enabled ->
                enabledState.update { enabled }
                if (enabled) loadPoster() else clearPoster()
            }
            .launchIn(viewModelScope)
    }

    private fun loadPoster() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val poster = getLivingPosterUseCase.getLivingPoster(
                    type = mediaType,
                    mediaId = mediaId,
                )
                posterState.update { poster }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Living poster unavailable for %s %d", mediaType.value, mediaId.value)
                }
            }
        }
    }

    private fun clearPoster() {
        loadJob?.cancel()
        posterState.update { null }
    }

    val state = combine(
        enabledState,
        posterState,
    ) { state ->
        LivingPosterState(
            enabled = state[0] as Boolean,
            poster = state[1] as LivingPoster?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
