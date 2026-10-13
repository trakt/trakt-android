package tv.trakt.trakt.core.summary.shows.features.trivia.usecases

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.trivia.Trivia
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.common.networking.api.v3.V3Api
import java.util.Locale

private const val SUMMARY_LIMIT = 3

internal class GetShowTriviaUseCase(
    private val v3Api: V3Api,
) {
    suspend fun getTrivia(showId: TraktId): Trivia {
        val response = v3Api.getShowTrivia(
            showId = showId,
            locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.US,
        )

        val summary = response.summary.orEmpty()
        val items = response.items.orEmpty()

        return Trivia(
            summary = summary
                .take(SUMMARY_LIMIT)
                .toImmutableList(),
            facts = items
                .map {
                    TriviaFact(
                        id = it.id,
                        category = it.category,
                        text = it.text,
                        order = it.order,
                        spoiler = it.spoiler,
                    )
                }
                .sortedBy { it.order }
                .toImmutableList(),
        )
    }
}
