package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.TriviaFact

@Immutable
@Serializable
data class V3TriviaResponse(
    val summary: List<String>?,
    val items: List<TriviaFact>?,
)
