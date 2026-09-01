package tv.trakt.trakt.core.home.sections.recommended.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationSource

@Immutable
@Serializable
internal data class RecommendedSource(
    val type: Type,
    val show: Show? = null,
    val movie: Movie? = null,
    @Serializable(ImmutableListSerializer::class)
    val subgenres: ImmutableList<Subgenre> = persistentListOf(),
) {
    internal enum class Type {
        Favorite,
        Activity,
        Subgenre,
        Unknown,
        ;

        companion object {
            fun fromValue(value: String?): Type =
                when (value) {
                    "favorite" -> Favorite
                    "activity" -> Activity
                    "subgenre" -> Subgenre
                    else -> Unknown
                }
        }
    }

    @Immutable
    @Serializable
    internal data class Subgenre(
        val id: Long,
        val name: String,
        val slug: String,
    )

    companion object {
        fun fromDto(dto: V3RecommendationSource): RecommendedSource =
            RecommendedSource(
                type = Type.fromValue(dto.type),
                show = dto.show?.let { Show.fromDto(it) },
                movie = dto.movie?.let { Movie.fromDto(it) },
                subgenres = dto.subgenres
                    .orEmpty()
                    .map {
                        Subgenre(
                            id = it.id,
                            name = it.name,
                            slug = it.slug,
                        )
                    }
                    .toImmutableList(),
            )
    }
}
