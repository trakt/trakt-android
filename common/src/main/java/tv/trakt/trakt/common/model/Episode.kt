package tv.trakt.trakt.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.serializers.InstantSerializer
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_FINALE
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_PREMIERE
import tv.trakt.trakt.common.networking.EpisodeCalendarDto
import tv.trakt.trakt.common.networking.EpisodeCalendarsDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.EpisodeLikesDto
import tv.trakt.trakt.common.networking.LastEpisodeDto
import tv.trakt.trakt.resources.R
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// Grace window before an episode's air date during which it is already
// considered aired. Absorbs timezone/scheduling skew so check-ins and
// other aired-gated UI don't lag behind the actual broadcast.
private const val AIR_BUFFER_HOURS = 24L

@Immutable
@Serializable
data class Episode(
    val ids: Ids,
    val type: EpisodeType?,
    val number: Int,
    val season: Int,
    val title: String,
    val numberAbs: Int?,
    val overview: String?,
    val rating: Rating,
    val commentCount: Int,
    val runtime: Duration?,
    val originalTitle: String,
    val images: Images?,
    @Serializable(InstantSerializer::class)
    val updatedAt: Instant?,
    @Serializable(InstantSerializer::class)
    private val firstAired: Instant?,
    @Serializable(InstantSerializer::class)
    private val effectiveReleaseDate: Instant?,
) {
    val releasedAt: Instant?
        get() = effectiveReleaseDate ?: firstAired

    val isReleased: Boolean
        get() = releasedAt?.let {
            !it.isAfter(nowUtcInstant().plus(AIR_BUFFER_HOURS, ChronoUnit.HOURS))
        } ?: false

    val seasonEpisode: SeasonEpisode
        get() = SeasonEpisode(
            season = season,
            episode = number,
        )

    @Composable
    fun rememberReleased(): Boolean {
        return remember(firstAired, effectiveReleaseDate) {
            isReleased
        }
    }

    @Composable
    fun seasonEpisodeString(): String {
        val string = stringResource(
            R.string.episode_footer_season_episode,
            this.season,
            this.number,
        )
        return when {
            title.isNotBlank() -> "$string - $title"
            else -> string
        }
    }

    @Composable
    fun isPremiere(isLatestAired: Boolean = false): Boolean =
        remember(type, isLatestAired) {
            if (type?.isPremiere == true) return@remember true
            type == MID_SEASON_PREMIERE && isLatestAired
        }

    @Composable
    fun isFinale(isLatestAired: Boolean = false): Boolean =
        remember(type, isLatestAired) {
            if (type?.isFinale == true) return@remember true
            type == MID_SEASON_FINALE && isLatestAired
        }
}

fun Episode.Companion.fromDto(dto: EpisodeDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        type = dto.episodeType?.let { EpisodeType.fromValue(it.value) },
        number = dto.number,
        season = dto.season,
        title = dto.title ?: "N/A",
        numberAbs = dto.numberAbs,
        overview = dto.overview,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        commentCount = dto.commentCount ?: 0,
        runtime = dto.runtime?.minutes,
        originalTitle = dto.originalTitle ?: "",
        images = Images(
            screenshot = (dto.images?.screenshot ?: emptyList()).toImmutableList(),
        ),
        firstAired = dto.firstAired?.toInstant(),
        effectiveReleaseDate = dto.effectiveReleaseDate?.toInstant(),
        updatedAt = dto.updatedAt?.toInstant(),
    )
}

fun Episode.Companion.fromDto(dto: LastEpisodeDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        type = dto.episodeType?.let { EpisodeType.fromValue(it.value) },
        number = dto.number,
        season = dto.season,
        title = dto.title ?: "N/A",
        numberAbs = dto.numberAbs,
        overview = dto.overview,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        commentCount = dto.commentCount ?: 0,
        runtime = dto.runtime?.minutes,
        originalTitle = dto.originalTitle ?: "",
        images = Images(
            screenshot = (dto.images?.screenshot ?: emptyList()).toImmutableList(),
        ),
        firstAired = dto.firstAired?.toInstant(),
        effectiveReleaseDate = dto.effectiveReleaseDate?.toInstant(),
        updatedAt = dto.updatedAt?.toInstant(),
    )
}

fun Episode.Companion.fromDto(dto: EpisodeLikesDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        type = dto.episodeType?.let { EpisodeType.fromValue(it.value) },
        number = dto.number,
        season = dto.season,
        title = dto.title ?: "N/A",
        numberAbs = dto.numberAbs,
        overview = dto.overview,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        commentCount = dto.commentCount ?: 0,
        runtime = dto.runtime?.minutes,
        originalTitle = dto.originalTitle ?: "",
        images = Images(
            screenshot = (dto.images?.screenshot ?: emptyList()).toImmutableList(),
        ),
        firstAired = dto.firstAired?.toInstant(),
        effectiveReleaseDate = dto.effectiveReleaseDate?.toInstant(),
        updatedAt = dto.updatedAt?.toInstant(),
    )
}

fun Episode.Companion.fromDto(dto: EpisodeCalendarsDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        type = dto.episodeType?.let { EpisodeType.fromValue(it.value) },
        number = dto.number,
        season = dto.season,
        title = dto.title ?: "N/A",
        numberAbs = dto.numberAbs,
        overview = dto.overview,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        commentCount = dto.commentCount ?: 0,
        runtime = dto.runtime?.minutes,
        originalTitle = dto.originalTitle ?: "",
        images = Images(
            screenshot = (dto.images?.screenshot ?: emptyList()).toImmutableList(),
        ),
        firstAired = dto.firstAired?.toInstant(),
        effectiveReleaseDate = dto.effectiveReleaseDate?.toInstant(),
        updatedAt = dto.updatedAt?.toInstant(),
    )
}

fun Episode.Companion.fromDto(dto: EpisodeCalendarDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        type = dto.episodeType?.let { EpisodeType.fromValue(it.value) },
        number = dto.number,
        season = dto.season,
        title = dto.title ?: "N/A",
        numberAbs = dto.numberAbs,
        overview = dto.overview,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        commentCount = dto.commentCount ?: 0,
        runtime = dto.runtime?.minutes,
        originalTitle = dto.originalTitle ?: "",
        images = Images(
            screenshot = (dto.images?.screenshot ?: emptyList()).toImmutableList(),
        ),
        firstAired = dto.firstAired?.toInstant(),
        effectiveReleaseDate = dto.effectiveReleaseDate?.toInstant(),
        updatedAt = dto.updatedAt?.toInstant(),
    )
}
