package tv.trakt.trakt.core.summary.movies.features.sentiment.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieSentimentUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getSentiments(movieId: TraktId): Sentiments {
        val sentimentsResponse = remoteSource.getSentiments(movieId)
        return Sentiments(
            overall = Sentiments.Overall.fromValue(sentimentsResponse?.sentiment?.overall),
            analysis = sentimentsResponse?.analysis.orEmpty(),
            highlight = sentimentsResponse?.highlight.orEmpty(),
            pros = sentimentsResponse?.aspect?.pros
                ?.map {
                    Sentiments.Theme(
                        value = it.theme,
                        confidence = it.confidence,
                    )
                }
                ?.sortedByDescending { it.confidence }
                ?.toImmutableList()
                ?: EmptyImmutableList,
            cons = sentimentsResponse?.aspect?.cons
                ?.map {
                    Sentiments.Theme(
                        value = it.theme,
                        confidence = it.confidence,
                    )
                }
                ?.sortedByDescending { it.confidence }
                ?.toImmutableList()
                ?: EmptyImmutableList,
        )
    }
}
