package tv.trakt.trakt.core.summary.livingposter

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.livingposter.LivingPoster

@Immutable
internal data class LivingPosterState(
    val enabled: Boolean = false,
    val poster: LivingPoster? = null,
)
