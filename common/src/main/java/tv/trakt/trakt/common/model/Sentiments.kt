package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class Sentiments(
    val good: ImmutableList<Sentiment> = emptyList<Sentiment>().toImmutableList(),
    val bad: ImmutableList<Sentiment> = emptyList<Sentiment>().toImmutableList(),
) {
    data class Sentiment(
        val sentiment: String,
    )
}
