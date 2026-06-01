package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User

@Immutable
internal data class SentimentState(
    val user: User? = null,
    val sentiments: Sentiments,
    val mediaTitle: String? = null,
    val backgroundUrl: String? = null,
)
