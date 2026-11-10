package tv.trakt.trakt.core.today.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.common.helpers.extensions.stripMarkdownLinks
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieRatingsUseCase
import tv.trakt.trakt.core.summary.sentiment.usecases.GetSentimentUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase

internal class GetTodayDetailsUseCase(
    private val getMovieRatingsUseCase: GetMovieRatingsUseCase,
    private val getShowRatingsUseCase: GetShowRatingsUseCase,
    private val getSentimentUseCase: GetSentimentUseCase,
) {
    suspend fun getRatings(media: TodayMedia): ExternalRating {
        return when (media) {
            is TodayMedia.MovieMedia -> getMovieRatingsUseCase.getExternalRatings(media.ids.trakt)
            is TodayMedia.ShowMedia -> getShowRatingsUseCase.getExternalRatings(media.ids.trakt)
        }
    }

    suspend fun getSentiment(media: TodayMedia): Sentiments? {
        val sentiment = when (media) {
            is TodayMedia.MovieMedia -> getSentimentUseCase.getSentiments(media.ids.trakt, MediaType.Movie)
            is TodayMedia.ShowMedia -> getSentimentUseCase.getSentiments(media.ids.trakt, MediaType.Show)
        }

        if (sentiment.isEmpty) return null

        return sentiment.copy(
            highlight = sentiment.highlight.stripMarkdownLinks(),
            pros = sentiment.pros.stripMarkdownLinks(),
            cons = sentiment.cons.stripMarkdownLinks(),
        )
    }

    private fun List<Sentiments.Theme>.stripMarkdownLinks(): ImmutableList<Sentiments.Theme> {
        return map { it.copy(value = it.value.stripMarkdownLinks()) }.toImmutableList()
    }
}
