package tv.trakt.trakt.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.EpisodeLikesDto
import tv.trakt.trakt.common.networking.LastEpisodeDto
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime
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
    val firstAired: ZonedDateTime?,
    val updatedAt: ZonedDateTime?,
) {
    companion object

    val seasonEpisode: SeasonEpisode
        get() = SeasonEpisode(
            season = season,
            episode = number,
        )

    @Composable
    fun seasonEpisodeString(): String {
        val string = stringResource(R.string.episode_footer_season_episode, this.season, this.number)
        return when {
            title.isNotBlank() -> "$string - $title"
            else -> string
        }
    }
}

fun Episode.Companion.fromDto(dto: EpisodeDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        number = dto.number,
        season = dto.season,
        title = dto.title,
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
        firstAired = dto.firstAired?.toZonedDateTime(),
        updatedAt = dto.updatedAt?.toZonedDateTime(),
    )
}

fun Episode.Companion.fromDto(dto: LastEpisodeDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        number = dto.number,
        season = dto.season,
        title = dto.title,
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
        firstAired = dto.firstAired?.toZonedDateTime(),
        updatedAt = dto.updatedAt?.toZonedDateTime(),
    )
}

fun Episode.Companion.fromDto(dto: EpisodeLikesDto): Episode {
    return Episode(
        ids = Ids.fromDto(dto.ids),
        number = dto.number,
        season = dto.season,
        title = dto.title,
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
        firstAired = dto.firstAired?.toZonedDateTime(),
        updatedAt = dto.updatedAt?.toZonedDateTime(),
    )
}
