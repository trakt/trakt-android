package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Person.Companion
import tv.trakt.trakt.common.networking.PersonDetailsDto
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonSearchDto

@Immutable
@Serializable
data class Person(
    val ids: Ids,
    val name: String,
    val biography: String?,
    val images: Images?,
    val knownForDepartment: String?,
) {
    companion object
}

fun Companion.fromDto(dto: PersonDto): Person {
    return Person(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = dto.ids.slug.toSlugId(),
            imdb = dto.ids.imdb?.let { ImdbId(it) },
            tmdb = dto.ids.tmdb?.let { TmdbId(it) },
        ),
        name = dto.name,
        biography = null,
        knownForDepartment = null,
        images = dto.images?.let {
            Images(
                headshot = it.headshot.toImmutableList(),
            )
        },
    )
}

fun Companion.fromDto(dto: PersonDetailsDto): Person {
    return Person(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = dto.ids.slug.toSlugId(),
            imdb = dto.ids.imdb?.let { ImdbId(it) },
            tmdb = dto.ids.tmdb?.let { TmdbId(it) },
        ),
        name = dto.name,
        biography = dto.biography,
        knownForDepartment = dto.knownForDepartment,
        images = dto.images?.let {
            Images(
                headshot = it.headshot.toImmutableList(),
            )
        },
    )
}

fun Companion.fromDto(dto: PersonSearchDto): Person {
    return Person(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = dto.ids.slug.toSlugId(),
            imdb = dto.ids.imdb?.let { ImdbId(it) },
            tmdb = dto.ids.tmdb?.let { TmdbId(it) },
        ),
        name = dto.name,
        biography = dto.biography,
        knownForDepartment = dto.knownForDepartment,
        images = dto.images?.let {
            Images(
                headshot = it.headshot.toImmutableList(),
            )
        },
    )
}
