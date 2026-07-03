package tv.trakt.trakt.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.helpers.serializers.InstantSerializer
import tv.trakt.trakt.common.model.MediaStatus.Released
import tv.trakt.trakt.common.model.Show.Companion
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.ShowDto
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
@Serializable
data class Show(
    val ids: Ids,
    val title: String,
    val titleOriginal: String?,
    val overview: String?,
    val network: String?,
    val status: MediaStatus?,
    val year: Int?,
    @Serializable(ImmutableListSerializer::class)
    val genres: ImmutableList<MediaGenre>,
    val images: Images?,
    val colors: MediaColors?,
    val rating: Rating,
    val certification: String?,
    val trailer: String?,
    val runtime: Duration?,
    val totalRuntime: Duration?,
    val airedEpisodes: Int,
    val country: String?,
    @Serializable(ImmutableListSerializer::class)
    val languages: ImmutableList<String>,
    @Serializable(InstantSerializer::class)
    val releasedAt: Instant?,
) {
    companion object

    val isReleased: Boolean
        get() = status == Released ||
            releasedAt?.let { !it.isAfter(nowUtcInstant()) } ?: false

    @Composable
    fun rememberReleased(): Boolean {
        return remember(releasedAt, status) {
            isReleased
        }
    }
}

fun Companion.fromDto(dto: ShowDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
        overview = dto.overview,
        year = dto.year,
        status = MediaStatus.fromSlug(dto.status),
        releasedAt = dto.firstAired?.toInstant(),
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
        runtime = dto.runtime?.minutes,
        totalRuntime = dto.totalRuntime?.minutes,
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
    )
}

fun Companion.fromDto(dto: RecommendedShowDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
        overview = dto.overview,
        year = dto.year,
        releasedAt = dto.firstAired?.toInstant(),
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
        runtime = dto.runtime?.minutes,
        totalRuntime = dto.totalRuntime?.minutes,
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
    )
}

fun Companion.fromDto(dto: ShowCalendarsDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
        overview = dto.overview,
        year = dto.year,
        releasedAt = dto.firstAired?.toInstant(),
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
        runtime = dto.runtime?.minutes,
        totalRuntime = dto.totalRuntime?.minutes,
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
    )
}
