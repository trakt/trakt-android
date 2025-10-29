package tv.trakt.trakt.core.shows.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show

internal const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal interface GetPopularShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<Show>

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<Show>
}
