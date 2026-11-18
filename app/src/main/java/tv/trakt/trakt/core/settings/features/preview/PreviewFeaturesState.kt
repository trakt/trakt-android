package tv.trakt.trakt.core.settings.features.preview

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.featureflags.model.FeatureFlagPreview

@Immutable
internal data class PreviewFeaturesState(
    val features: ImmutableList<FeatureFlagPreview>? = null,
)
