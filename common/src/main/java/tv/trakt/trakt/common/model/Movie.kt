package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toHttpsUrl
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.helpers.serializers.LocalDateSerializer
import tv.trakt.trakt.common.model.Movie.Companion
import tv.trakt.trakt.common.networking.MovieCalendarDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
@Serializable
data class Movie(
    val ids: Ids,
    val title: String,
    val titleOriginal: String?,
    val overview: String?,
    @Serializable(LocalDateSerializer::class)
    val released: LocalDate?,
    val year: Int?,
    val trailer: String?,
    @Serializable(ImmutableListSerializer::class)
    val genres: ImmutableList<MediaGenre>,
    val images: Images?,
    val colors: MediaColors?,
    val rating: Rating,
    val certification: String?,
    val status: MediaStatus?,
    val runtime: Duration?,
    val country: String?,
    @Serializable(ImmutableListSerializer::class)
    val languages: ImmutableList<String>,
    val homepage: String? = null,
    val socialIds: SocialIds? = null,
    @Serializable(ImmutableListSerializer::class)
    val postCredits: ImmutableList<PostCreditsScene> = persistentListOf(),
) {
    companion object

    val titleNormalized: String
        get() = title.trim()
            .lowercase()
            .removePrefix("the ")
            .removePrefix("a ")
            .removePrefix("an ")

    // Considered released if status is "released" or released date is today or before
    val isReleased: Boolean
        get() {
            return status == MediaStatus.Released ||
                released?.isTodayOrBefore() == true
        }

    val isNew: Boolean
        get() {
            val released = released ?: return false
            val today = nowLocalDay()
            if (released.isAfter(today)) return status == MediaStatus.Released
            return !released.plusDays(NEW_RELEASE_WINDOW.toDays()).isBefore(today)
        }

    val yearString: String =
        (released?.year ?: year)?.toString() ?: "TBA"
}

fun Companion.fromDto(dto: MovieDto): Movie {
    return Movie(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
        overview = dto.overview,
        year = dto.year,
        released = dto.released?.let { LocalDate.parse(it) },
        genres = (dto.genres ?: listOf())
            .mapNotNull { MediaGenre.fromSlug(it) }
            .toImmutableList(),
        images = dto.images?.let { Images.fromDto(it) },
        colors = dto.colors?.poster?.let {
            MediaColors(
                Pair(
                    Color(it.getOrElse(0) { "#00000000" }.toColorInt()),
                    Color(it.getOrElse(1) { "#00000000" }.toColorInt()),
                ),
            )
        },
        certification = dto.certification,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        status = MediaStatus.fromSlug(dto.status),
        runtime = dto.runtime?.minutes,
        trailer = dto.trailer,
        postCredits = PostCreditsScene.fromFlags(
            during = dto.duringCredits,
            after = dto.afterCredits,
        ),
        country = dto.country,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
    )
}

fun Companion.fromDto(dto: RecommendedMovieDto): Movie {
    return Movie(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
        overview = dto.overview,
        year = dto.year,
        released = dto.released?.let { LocalDate.parse(it) },
        genres = (dto.genres ?: listOf())
            .mapNotNull { MediaGenre.fromSlug(it) }
            .toImmutableList(),
        images = dto.images?.let { Images.fromDto(it) },
        colors = dto.colors?.poster?.let {
            MediaColors(
                Pair(
                    Color(it.getOrElse(0) { "#00000000" }.toColorInt()),
                    Color(it.getOrElse(1) { "#00000000" }.toColorInt()),
                ),
            )
        },
        certification = dto.certification,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        runtime = dto.runtime?.minutes,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        country = dto.country,
        postCredits = PostCreditsScene.fromFlags(
            during = dto.duringCredits,
            after = dto.afterCredits,
        ),
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
    )
}

fun Companion.fromDto(dto: MovieCalendarDto): Movie {
    return Movie(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
        overview = dto.overview,
        year = dto.year,
        released = dto.released?.let { LocalDate.parse(it) },
        genres = (dto.genres ?: listOf())
            .mapNotNull { MediaGenre.fromSlug(it) }
            .toImmutableList(),
        images = dto.images?.let { Images.fromDto(it) },
        colors = dto.colors?.poster?.let {
            MediaColors(
                Pair(
                    Color(it.getOrElse(0) { "#00000000" }.toColorInt()),
                    Color(it.getOrElse(1) { "#00000000" }.toColorInt()),
                ),
            )
        },
        certification = dto.certification,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        runtime = dto.runtime?.minutes,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        country = dto.country,
        postCredits = PostCreditsScene.fromFlags(
            during = dto.duringCredits,
            after = dto.afterCredits,
        ),
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
    )
}
