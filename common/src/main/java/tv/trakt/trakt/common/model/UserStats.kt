package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.UserStatsDto

@Immutable
data class UserStats(
    val totalMinutes: Int,
    val totalPlays: Int,
    val moviesWatched: Int = 0,
    val showsWatched: Int = 0,
    val episodesWatched: Int = 0,
    val ratings: Int = 0,
    val comments: Int = 0,
    val lists: Int? = null,
    val progressStarted: Int? = null,
    val progressFinished: Int? = null,
    val progressDropped: Int? = null,
) {
    companion object {
        fun fromDto(dto: UserStatsDto): UserStats {
            return UserStats(
                totalMinutes = dto.totalMinutes ?: (dto.movies.minutes + dto.episodes.minutes),
                totalPlays = dto.totalPlays ?: (dto.movies.plays + dto.episodes.plays),
                moviesWatched = dto.movies.watched,
                showsWatched = dto.shows.watched,
                episodesWatched = dto.episodes.watched,
                ratings = dto.ratings.total,
                comments = dto.movies.comments + dto.shows.comments + dto.seasons.comments + dto.episodes.comments,
                lists = dto.lists,
                progressStarted = dto.progress?.started,
                progressFinished = dto.progress?.finished,
                progressDropped = dto.progress?.dropped,
            )
        }
    }
}
