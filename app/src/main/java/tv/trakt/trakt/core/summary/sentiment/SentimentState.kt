package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.RealityCheckSection

@Immutable
internal data class SentimentState(
    val user: User? = null,
    val sentiments: Sentiments? = null,
    val realityCheck: RealityCheck? = null,
    val tabs: ImmutableList<InsightsTab> = EmptyImmutableList,
    val tab: InsightsTab = InsightsTab.RealityCheck,
    val collapsedSections: ImmutableSet<RealityCheckSection> = persistentSetOf(),
    val mediaTitle: String? = null,
    val backgroundUrl: String? = null,
)
