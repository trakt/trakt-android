package tv.trakt.trakt.core.summary.movies.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId

internal class MovieRatingsStorage : MovieRatingsLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, ExternalRating>()

    override suspend fun addRatings(
        movieId: TraktId,
        ratings: ExternalRating,
    ) {
        mutex.withLock {
            storage[movieId] = ratings
        }
    }

    override suspend fun getRatings(movieId: TraktId): ExternalRating? {
        return mutex.withLock {
            storage[movieId]
        }
    }
}
