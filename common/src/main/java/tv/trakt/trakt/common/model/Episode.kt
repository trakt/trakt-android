package tv.trakt.trakt.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.EpisodeLikesDto
import tv.trakt.trakt.common.networking.LastEpisodeDto
import tv.trakt.trakt.resources.R
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
data class Episode(
    val ids: Ids,
    val number: Int,
    val season: Int,
    val title: String,
    val numberAbs: Int?,
    val overview: String?,
    val rating: Rating,
    val commentCount: Int,
    val runtime: Duration?,
    val episodeType: String?,
    val originalTitle: String,
    val images: Images?,
    val updatedAt: Instant?,
    private val firstAired: Instant?,
    private val effectiveReleaseDate: Instant?,
) {
    companion object

    val releasedAt: Instant?
        get() = effectiveReleaseDate ?: firstAired

    val isReleased: Boolean
        get() = releasedAt?.let { !it.isAfter(nowUtcInstant()) } ?: false

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
        val string = stringResource(R.string.episode_footer_season_episode, this.season, this.number)
        return when {
            title.isNotBlank() -> "$string - $title"
            else -> string
        }
    }

    @Composable
    fun isPremiere(isLatestAired: Boolean? = null): Boolean =
        remember(episodeType, isLatestAired) {
            val premiere = episodeType?.contains("premiere") == true
            premiere && !isMidSeasonHidden(isLatestAired)
        }

    @Composable
    fun isFinale(isLatestAired: Boolean? = null): Boolean =
        remember(episodeType, isLatestAired) {
            val finale = episodeType?.contains("finale") == true
            finale && !isMidSeasonHidden(isLatestAired)
        }

    private fun isMidSeasonHidden(isLatestAired: Boolean?): Boolean {
        if (isLatestAired != false) return false
        return episodeType?.contains("mid_season") == true
    }

    val episodeTypeStringRes: Int?
        get() = when (episodeType) {
            "series_premiere" -> R.string.tag_text_series_premiere
            "season_premiere" -> R.string.tag_text_season_premiere
            "mid_season_premiere" -> R.string.tag_text_mid_season_premiere
            "series_finale" -> R.string.tag_text_series_finale
            "season_finale" -> R.string.tag_text_season_finale
            "mid_season_finale" -> R.string.tag_text_mid_season_finale
            else -> null
        }
}

fun Episode.Companion.fromDto(dto: EpisodeDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
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
        episodeType = dto.episodeType?.value,
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
        episodeType = dto.episodeType?.value,
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
        episodeType = dto.episodeType?.value,
        originalTitle = dto.originalTitle ?: "",
        images = Images(
            screenshot = (dto.images?.screenshot ?: emptyList()).toImmutableList(),
        ),
        firstAired = dto.firstAired?.toInstant(),
        effectiveReleaseDate = dto.effectiveReleaseDate?.toInstant(),
        updatedAt = dto.updatedAt?.toInstant(),
    )
}
