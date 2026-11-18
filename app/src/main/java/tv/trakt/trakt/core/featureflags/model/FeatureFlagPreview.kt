package tv.trakt.trakt.core.featureflags.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class FeatureFlagPreview(
    val flag: FeatureFlag,
    val audience: FeatureFlagAudience,
    val enabled: Boolean,
    val locked: Boolean,
)
