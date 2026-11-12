package tv.trakt.trakt.core.summary.sentiment.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.realitycheck.RealityCheck

@Immutable
internal data class MediaInsights(
    val sentiments: Sentiments?,
    val realityCheck: RealityCheck?,
    val tab: InsightsTab,
)
