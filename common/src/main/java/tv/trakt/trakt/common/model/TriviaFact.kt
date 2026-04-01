package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class TriviaFact(
    @SerialName("fact_id") val id: String,
    val category: String,
    val text: String,
    val order: Int,
    val spoiler: Boolean,
)
