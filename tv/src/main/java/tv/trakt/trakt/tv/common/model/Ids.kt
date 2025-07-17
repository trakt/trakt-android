package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.tv.networking.openapi.EpisodeIdsDto
import tv.trakt.trakt.tv.networking.openapi.MovieIdsDto
import tv.trakt.trakt.tv.networking.openapi.ShowIdsDto

@Immutable
@Serializable
internal data class Ids(
    val trakt: TraktId,
    val slug: SlugId,
    val tvdb: TvdbId? = null,
    val tmdb: TmdbId? = null,
    val imdb: ImdbId? = null,
    val plex: SlugId? = null,
) {
    companion object {
        fun fromDto(dto: ShowIdsDto): Ids {
            return Ids(
                trakt = TraktId(dto.trakt),
                slug = SlugId(dto.slug),
                tvdb = dto.tvdb?.let { TvdbId(it) },
                tmdb = dto.tmdb?.let { TmdbId(it) },
                imdb = dto.imdb?.let { ImdbId(it) },
                plex = dto.plex?.slug?.let { SlugId(it) },
            )
        }

        fun fromDto(dto: MovieIdsDto): Ids {
            return Ids(
                trakt = TraktId(dto.trakt),
                slug = SlugId(dto.slug),
                tmdb = dto.tmdb?.let { TmdbId(it) },
                imdb = dto.imdb?.let { ImdbId(it) },
                plex = dto.plex?.slug?.let { SlugId(it) },
                tvdb = null, // Movies do not have a TVDB ID
            )
        }

        fun fromDto(dto: EpisodeIdsDto): Ids {
            return Ids(
                trakt = TraktId(dto.trakt),
                slug = SlugId(""), // Episodes do not have a slug
                tmdb = dto.tmdb?.let { TmdbId(it) },
                imdb = dto.imdb?.let { ImdbId(it) },
                plex = dto.plex?.slug?.let { SlugId(it) },
                tvdb = dto.tvdb?.let { TvdbId(it) },
            )
        }
    }
}

@JvmInline
@Serializable
value class TraktId(
    val value: Int,
)

@JvmInline
@Serializable
value class TvdbId(
    val value: Int,
)

@JvmInline
@Serializable
value class TmdbId(
    val value: Int,
)

@JvmInline
@Serializable
value class ImdbId(
    val value: String,
)

@JvmInline
@Serializable
value class SlugId(
    val value: String,
)

fun Int.toTraktId(): TraktId = TraktId(this)

fun Int.toTvdbId(): TvdbId = TvdbId(this)

fun Int.toTmdbId(): TmdbId = TmdbId(this)

fun String.toImdbId(): ImdbId = ImdbId(this)

fun String.toSlugId(): SlugId = SlugId(this)
