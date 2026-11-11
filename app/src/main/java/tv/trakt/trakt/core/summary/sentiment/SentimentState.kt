package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab

@Immutable
internal data class SentimentState(
    val user: User? = null,
    val sentiments: Sentiments? = null,
    val realityCheck: RealityCheck? = null,
    val tabs: ImmutableList<InsightsTab> = EmptyImmutableList,
    val tab: InsightsTab = InsightsTab.RealityCheck,
    val mediaTitle: String? = null,
    val backgroundUrl: String? = null,
)
