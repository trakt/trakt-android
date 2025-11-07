package tv.trakt.trakt.core.summary.shows.features.sentiment.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource

internal class GetShowSentimentUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getSentiments(showId: TraktId): Sentiments {
        val sentiments = remoteSource.getSentiments(showId)
        return sentiments
            .copy(
                good = sentiments.good.take(3).toImmutableList(),
                bad = sentiments.bad.take(3).toImmutableList(),
            )
    }
}
