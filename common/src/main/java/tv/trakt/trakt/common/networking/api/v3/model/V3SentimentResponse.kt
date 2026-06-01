package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3SentimentResponse(
    val sentiment: V3Sentiment,
    val aspect: V3AspectSentiment,
    val analysis: String?,
    val highlight: String?,
) {
    @Immutable
    @Serializable
    data class V3Sentiment(
        val overall: String,
    )

    @Immutable
    @Serializable
    data class V3AspectSentiment(
        val pros: List<V3AspectSentimentTheme>,
        val cons: List<V3AspectSentimentTheme>,
    ) {
        @Immutable
        @Serializable
        data class V3AspectSentimentTheme(
            val theme: String,
            val confidence: Float,
        )
    }
}
