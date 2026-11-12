package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.MediaInsights

@Immutable
internal data class MediaSentimentState(
    val user: User? = null,
    val sentiment: Sentiments? = null,
    val realityCheck: RealityCheck? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
) {
    val hasInsights: Boolean
        get() = sentiment?.isEmpty == false || realityCheck != null

    fun insights(tab: InsightsTab = defaultTab): MediaInsights {
        return MediaInsights(
            sentiments = sentiment,
            realityCheck = realityCheck,
            tab = tab,
        )
    }

    private val defaultTab: InsightsTab
        get() = if (sentiment?.isEmpty == false) InsightsTab.Sentiment else InsightsTab.RealityCheck
}
