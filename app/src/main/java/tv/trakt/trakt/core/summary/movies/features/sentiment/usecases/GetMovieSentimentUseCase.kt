package tv.trakt.trakt.core.summary.movies.features.sentiment.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieSentimentUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getSentiments(movieId: TraktId): Sentiments {
        val sentiments = remoteSource.getSentiments(movieId)
        return sentiments
            .copy(
                good = sentiments.good.take(4).toImmutableList(),
                bad = sentiments.bad.take(4).toImmutableList(),
            )
    }
}
