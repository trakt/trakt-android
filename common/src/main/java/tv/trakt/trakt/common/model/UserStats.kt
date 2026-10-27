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
    val lists: Int = 0,
    val progressStarted: Int = 0,
    val progressFinished: Int = 0,
    val progressDropped: Int = 0,
) {
    companion object {
        fun fromDto(dto: UserStatsDto): UserStats {
            return UserStats(
                totalMinutes = dto.totalMinutes,
                totalPlays = dto.totalPlays,
                moviesWatched = dto.movies.watched,
                showsWatched = dto.shows.watched,
                episodesWatched = dto.episodes.watched,
                ratings = dto.ratings.total,
                comments = dto.movies.comments + dto.shows.comments + dto.seasons.comments + dto.episodes.comments,
                lists = dto.lists,
                progressStarted = dto.progress.started,
                progressFinished = dto.progress.finished,
                progressDropped = dto.progress.dropped,
            )
        }
    }
}
