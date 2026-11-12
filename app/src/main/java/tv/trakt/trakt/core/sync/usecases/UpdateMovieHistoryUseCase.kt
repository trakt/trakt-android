package tv.trakt.trakt.core.sync.usecases

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.RemoveOrphanedRatingUseCase

/**
 * Adding a play and removing a single play reload the movies progress before publishing the
 * change. Callers that refresh the progress on their own, without blocking the user, pass
 * `refreshProgress = false`.
 */
internal class UpdateMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
    private val orphanedRatings: RemoveOrphanedRatingUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToWatched(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ): PostSyncHistoryAdd200Response {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        val response = remoteSource.addToWatched(
            movieId = movieId,
            watchedAt = watchedAt,
        )

        if (refreshProgress) {
            loadProgressUseCase.loadMoviesProgress()
        }
        userWatchlistLocalSource.removeMovies(ids = setOf(movieId))
        userWatchlistMinLocalSource.removeMovies(ids = setOf(movieId))
        dataChanges.notify(Kind.History, Kind.Watchlist, origin = origin)

        return response
    }

    suspend fun removeAllFromHistory(
        movieId: TraktId,
        origin: ChangeOrigin? = null,
    ) {
        val orphanedRating = orphanedRatings.findMovie(movieId)
        remoteSource.removeAllFromHistory(
            movieId = movieId,
        )
        orphanedRatings.remove(orphanedRating)

        userProgressLocalSource.removeMovies(ids = setOf(movieId))
        dataChanges.notify(Kind.History, Kind.Progress, origin = origin)
    }

    suspend fun removePlayFromHistory(
        playId: Long,
        movieId: TraktId,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ) {
        val orphanedRating = orphanedRatings.findMovie(movieId)
        remoteSource.removeSingleFromHistory(
            playId = playId,
        )
        orphanedRatings.remove(orphanedRating)

        if (refreshProgress) {
            loadProgressUseCase.loadMoviesProgress()
        }
        dataChanges.notify(Kind.History, origin = origin)
    }
}
