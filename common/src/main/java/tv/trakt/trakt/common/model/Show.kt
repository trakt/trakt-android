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
import tv.trakt.trakt.common.helpers.extensions.toHttpsUrl
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.helpers.serializers.InstantSerializer
import tv.trakt.trakt.common.model.MediaStatus.Canceled
import tv.trakt.trakt.common.model.MediaStatus.Ended
import tv.trakt.trakt.common.model.MediaStatus.Released
import tv.trakt.trakt.common.model.Show.Companion
import tv.trakt.trakt.common.networking.PopularMediaDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowAirsDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.ShowDto
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
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
    val airs: Airs? = null,
    val homepage: String? = null,
    val socialIds: SocialIds? = null,
) {
    companion object

    val titleNormalized: String
        get() = title.trim().lowercase()
            .removePrefix("the ")
            .removePrefix("a ")
            .removePrefix("an ")

    val isReleased: Boolean
        get() = status == Released ||
            releasedAt?.let { !it.isAfter(nowUtcInstant()) } ?: false

    val isEnded: Boolean
        get() = status == Ended || status == Canceled

    val isAiring: Boolean
        get() = !isEnded && releasedAt?.let { !it.isAfter(nowUtcInstant()) } == true

    @Composable
    fun rememberReleased(): Boolean {
        return remember(releasedAt, status) {
            isReleased
        }
    }

    @Immutable
    @Serializable
    data class Airs(
        val day: String,
        val time: String,
        val timezone: String,
    ) {
        fun toZonedDateTime(
            zone: ZoneId = ZoneId.systemDefault(),
            now: Instant = Instant.now(),
        ): ZonedDateTime? {
            val dayOfWeek = runCatching { DayOfWeek.valueOf(day.uppercase()) }.getOrNull() ?: return null
            val localTime = runCatching { LocalTime.parse(time) }.getOrNull() ?: return null
            val broadcastZone = runCatching { ZoneId.of(timezone) }.getOrNull() ?: return null

            return now.atZone(broadcastZone)
                .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                .with(localTime)
                .withZoneSameInstant(zone)
        }

        companion object {
            fun fromDto(dto: ShowAirsDto): Airs? {
                val day = dto.day?.takeIf { it.isNotBlank() } ?: return null
                val time = dto.time?.takeIf { it.isNotBlank() } ?: return null
                val timezone = dto.timezone?.takeIf { it.isNotBlank() } ?: return null

                return Airs(
                    day = day,
                    time = time,
                    timezone = timezone,
                )
            }
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
        totalRuntime = totalRuntimeOf(
            totalRuntime = dto.totalRuntime,
            runtime = dto.runtime,
            airedEpisodes = dto.airedEpisodes,
        ),
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        airs = dto.airs?.let { Show.Airs.fromDto(it) },
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
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
        totalRuntime = totalRuntimeOf(
            totalRuntime = dto.totalRuntime,
            runtime = dto.runtime,
            airedEpisodes = dto.airedEpisodes,
        ),
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        airs = dto.airs?.let { Show.Airs.fromDto(it) },
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
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
        totalRuntime = totalRuntimeOf(
            totalRuntime = dto.totalRuntime,
            runtime = dto.runtime,
            airedEpisodes = dto.airedEpisodes,
        ),
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        airs = dto.airs?.let { Show.Airs.fromDto(it) },
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
    )
}

fun Companion.fromDto(dto: PopularMediaDto): Show {
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
        totalRuntime = totalRuntimeOf(
            totalRuntime = dto.totalRuntime,
            runtime = dto.runtime,
            airedEpisodes = dto.airedEpisodes,
        ),
        status = MediaStatus.fromSlug(dto.status),
        trailer = dto.trailer,
        airedEpisodes = dto.airedEpisodes ?: 0,
        country = dto.country,
        network = dto.network,
        languages = (dto.languages ?: emptyList()).toImmutableList(),
        airs = dto.airs?.let { Show.Airs.fromDto(it) },
        homepage = dto.homepage?.toHttpsUrl(),
        socialIds = dto.socialIds?.let { SocialIds.fromDto(it) },
    )
}

private fun totalRuntimeOf(
    totalRuntime: Int?,
    runtime: Int?,
    airedEpisodes: Int?,
): Duration? {
    val minutes = totalRuntime ?: runtime?.let { it * (airedEpisodes ?: 0) }
    return minutes?.takeIf { it > 0 }?.minutes
}
