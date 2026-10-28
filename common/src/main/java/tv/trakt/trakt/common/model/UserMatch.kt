package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.networking.api.v3.model.V3UserMatchResponse

/**
 * Taste compatibility between the signed-in user and another user.
 * Scores are whole percentages from 0 to 100.
 */
@Immutable
data class UserMatch(
    val score: Int,
    val topicsScore: Int,
    val favoritesScore: Int,
    val sharedTopics: ImmutableList<SharedTopic>,
    val sharedMovies: ImmutableList<TraktId>,
    val sharedShows: ImmutableList<TraktId>,
) {
    /**
     * @property rarity Corpus rarity from 0 to 1, higher is more niche. Null means no signal, not common.
     */
    @Immutable
    data class SharedTopic(
        val id: Int,
        val name: String,
        val rarity: Float?,
    )

    companion object {
        fun fromDto(dto: V3UserMatchResponse): UserMatch {
            return UserMatch(
                score = dto.score.coerceIn(0, 100),
                topicsScore = dto.breakdown.subgenres.coerceIn(0, 100),
                favoritesScore = dto.breakdown.favorites.coerceIn(0, 100),
                sharedTopics = dto.shared.subgenres
                    .map {
                        SharedTopic(
                            id = it.id,
                            name = it.name,
                            rarity = it.rarity,
                        )
                    }
                    .toImmutableList(),
                sharedMovies = dto.shared.favorites.movies
                    .map(::TraktId)
                    .toImmutableList(),
                sharedShows = dto.shared.favorites.shows
                    .map(::TraktId)
                    .toImmutableList(),
            )
        }
    }
}
