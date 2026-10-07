package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.UserMediaDto

@Immutable
@Serializable
data class V3LeaderboardEntryResponse(
    val rank: Int?,
    val user: UserMediaDto,
    @SerialName("total_minutes")
    val totalMinutes: Int?,
    @SerialName("total_plays")
    val totalPlays: Int?,
    val locked: Boolean,
)
