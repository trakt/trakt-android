package tv.trakt.trakt.core.shows.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show

internal const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal interface GetRecommendedShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<Show>

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        skipLocal: Boolean = false,
    ): ImmutableList<Show>
}
