package tv.trakt.trakt.common.model.trivia

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Trivia(
    val summary: ImmutableList<String>,
    val facts: ImmutableList<TriviaFact>,
)

@Immutable
@Serializable
data class TriviaFact(
    @SerialName("fact_id") val id: String,
    val category: String,
    val text: String,
    val order: Int,
    val spoiler: Boolean,
)
