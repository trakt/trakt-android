package tv.trakt.trakt.core.summary.sentiment.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId

internal class GetSentimentUseCase(
    private val moviesRemoteSource: MoviesRemoteSource,
    private val showsRemoteSource: ShowsRemoteSource,
) {
    suspend fun getSentiments(
        mediaId: TraktId,
        mediaType: MediaType,
    ): Sentiments {
        val sentimentsResponse = when (mediaType) {
            MediaType.Movie -> moviesRemoteSource.getSentiments(mediaId)
            MediaType.Show -> showsRemoteSource.getSentiments(mediaId)
            MediaType.Season, MediaType.Episode -> error("Sentiment is not available for $mediaType")
        }

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
