package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.common.model.TmdbId
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.PersonDetailsDto
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.tv.common.model.Person.Companion

@Immutable
internal data class Person(
    val ids: Ids,
    val name: String,
    val biography: String?,
    val images: Images?,
) {
    companion object
}

internal fun Companion.fromDto(dto: PersonDto): Person {
    return Person(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = dto.ids.slug.toSlugId(),
            imdb = dto.ids.imdb?.let { ImdbId(it) },
            tmdb = dto.ids.tmdb?.let { TmdbId(it) },
        ),
        name = dto.name,
        biography = null,
        images = dto.images?.let {
            Images(
                headshot = it.headshot.toImmutableList(),
            )
        },
    )
}

internal fun Companion.fromDto(dto: PersonDetailsDto): Person {
    return Person(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = dto.ids.slug.toSlugId(),
            imdb = dto.ids.imdb?.let { ImdbId(it) },
            tmdb = dto.ids.tmdb?.let { TmdbId(it) },
        ),
        name = dto.name,
        biography = dto.biography,
        images = dto.images?.let {
            Images(
                headshot = it.headshot.toImmutableList(),
            )
        },
    )
}
