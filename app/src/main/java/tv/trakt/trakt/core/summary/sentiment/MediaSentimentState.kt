package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMatch
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.DocumentaryCheck
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.MediaInsights
import tv.trakt.trakt.core.summary.ui.views.insights.InsightsMatch

@Immutable
internal data class MediaSentimentState(
    val user: User? = null,
    val sentiment: Sentiments? = null,
    val realityCheck: RealityCheck? = null,
    val documentaryCheck: DocumentaryCheck? = null,
    val match: MediaMatch? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
) {
    val hasInsights: Boolean
        get() = sentiment?.isEmpty == false || realityCheck != null || documentaryCheck != null

    val matchScore: Int?
        get() = match?.score

    // The match route answers only VIP and director accounts, so only they see the empty ring.
    private val hasMatchRing: Boolean
        get() = matchScore != null || user?.isAnyVip == true || user?.isDirector == true

    val insightsMatch: InsightsMatch?
        get() = when {
            hasMatchRing -> InsightsMatch(
                score = matchScore,
                avatarUrl = user?.images?.avatar?.full,
            )
            else -> null
        }

    val hasDrawer: Boolean
        get() = hasInsights || matchScore != null

    fun insights(tab: InsightsTab = defaultTab): MediaInsights {
        return MediaInsights(
            sentiments = sentiment,
            realityCheck = realityCheck,
            documentaryCheck = documentaryCheck,
            match = match?.takeIf { it.score != null },
            tab = tab,
        )
    }

    private val defaultTab: InsightsTab
        get() = when {
            sentiment?.isEmpty == false -> InsightsTab.Sentiment
            realityCheck != null || documentaryCheck != null -> InsightsTab.RealityCheck
            else -> InsightsTab.Match
        }
}
