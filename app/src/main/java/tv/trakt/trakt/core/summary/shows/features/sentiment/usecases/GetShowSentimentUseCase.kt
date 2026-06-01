package tv.trakt.trakt.core.summary.shows.features.sentiment.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowSentimentUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getSentiments(showId: TraktId): Sentiments {
        val sentimentsResponse = remoteSource.getSentiments(showId)
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
