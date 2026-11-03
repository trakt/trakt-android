package tv.trakt.trakt.common.model.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationPeopleReason

@Immutable
data class RecommendedPerson(
    val person: Person,
    val watchedRoles: ImmutableList<Role>,
    val watchedCount: Int,
    val titles: ImmutableList<Title>,
) {
    enum class Role {
        Directing,
        Making,
        Acting,
        ;

        companion object {
            fun fromValue(value: String): Role? =
                when (value) {
                    "directing" -> Directing
                    "making" -> Making
                    "acting" -> Acting
                    else -> null
                }
        }
    }

    @Immutable
    data class Title(
        val movie: Movie? = null,
        val show: Show? = null,
    )

    companion object {
        fun fromDto(dto: V3RecommendationPeopleReason): RecommendedPerson =
            RecommendedPerson(
                person = Person.fromDto(dto.person),
                watchedRoles = dto.watchedRoles
                    .orEmpty()
                    .mapNotNull(Role::fromValue)
                    .toImmutableList(),
                watchedCount = dto.watchedCount,
                titles = dto.titles
                    .orEmpty()
                    .map {
                        Title(
                            movie = it.movie?.let { movie -> Movie.fromDto(movie) },
                            show = it.show?.let { show -> Show.fromDto(show) },
                        )
                    }
                    .toImmutableList(),
            )
    }
}
