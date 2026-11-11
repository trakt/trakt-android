package tv.trakt.trakt.core.summary.sentiment.model

import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.resources.R

internal enum class InsightsTab(
    val value: String,
    val displayTextRes: Int,
    val upsellSource: VipUpsellSource,
) {
    Sentiment("sentiment", R.string.header_community_sentiment, VipUpsellSource.Sentiment),
    RealityCheck("reality-check", R.string.header_reality_check, VipUpsellSource.RealityCheck),
}
