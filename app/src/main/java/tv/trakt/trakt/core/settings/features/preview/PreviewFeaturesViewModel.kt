package tv.trakt.trakt.core.settings.features.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.model.FeatureFlagPreview
import tv.trakt.trakt.core.featureflags.usecases.FeatureFlagsUseCase

@Suppress("UNCHECKED_CAST")
internal class PreviewFeaturesViewModel(
    private val featureFlagsUseCase: FeatureFlagsUseCase,
) : ViewModel() {
    private val initialState = PreviewFeaturesState()

    private val featuresState = MutableStateFlow(initialState.features)

    init {
        observeFeatures()
        markFeaturesSeen()
    }

    private fun observeFeatures() {
        featureFlagsUseCase.observePreviews()
            .onEach { features ->
                featuresState.update { features }
            }
            .launchIn(viewModelScope)
    }

    private fun markFeaturesSeen() {
        viewModelScope.launch {
            featureFlagsUseCase.markPreviewsSeen()
        }
    }

    fun setFeatureEnabled(
        flag: FeatureFlag,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            featureFlagsUseCase.setEnabled(flag, enabled)
        }
    }

    val state = combine(
        featuresState,
    ) { state ->
        PreviewFeaturesState(
            features = state[0] as ImmutableList<FeatureFlagPreview>?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
