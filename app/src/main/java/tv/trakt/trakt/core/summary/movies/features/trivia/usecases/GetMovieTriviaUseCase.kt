package tv.trakt.trakt.core.summary.movies.features.trivia.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.replaceMarkdown
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.trivia.Trivia
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.common.networking.api.v3.V3Api

private const val SUMMARY_LIMIT = 3

internal class GetMovieTriviaUseCase(
    private val v3Api: V3Api,
) {
    suspend fun getTrivia(movieId: TraktId): Trivia {
        val response = v3Api.getMovieTrivia(movieId)

        val summary = response.summary.orEmpty()
        val items = response.items.orEmpty()

        return Trivia(
            summary = summary
                .take(SUMMARY_LIMIT)
                .map { it.replaceMarkdown() }
                .toImmutableList(),
            facts = items
                .map {
                    TriviaFact(
                        id = it.id,
                        category = it.category,
                        text = it.text.replaceMarkdown(),
                        order = it.order,
                        spoiler = it.spoiler,
                    )
                }
                .sortedBy { it.order }
                .toImmutableList(),
        )
    }
}
