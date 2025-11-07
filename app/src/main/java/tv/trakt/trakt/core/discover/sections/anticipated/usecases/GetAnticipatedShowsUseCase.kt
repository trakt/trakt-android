package tv.trakt.trakt.core.discover.sections.anticipated.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.model.WatchersShow

internal const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal interface GetAnticipatedShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<WatchersShow>

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchersShow>
}
