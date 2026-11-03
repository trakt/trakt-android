package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.ShowDto

@Immutable
@Serializable
data class V3RecommendationPeopleReason(
    val person: PersonDto,
    val role: String,
    @SerialName("watched_roles")
    val watchedRoles: List<String>? = null,
    @SerialName("watched_count")
    val watchedCount: Int = 0,
    val titles: List<Title>? = null,
) {
    @Immutable
    @Serializable
    data class Title(
        val movie: MovieDto? = null,
        val show: ShowDto? = null,
    )
}
