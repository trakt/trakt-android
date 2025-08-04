package tv.trakt.trakt.app.core.details.lists.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto

internal interface ListsRemoteDataSource {
    suspend fun getShowListItems(
        listId: TraktId,
        limit: Int,
        page: Int = 1,
        extended: String = "images",
    ): List<ListShowItemDto>

    suspend fun getMovieListItems(
        listId: TraktId,
        limit: Int,
        page: Int = 1,
        extended: String = "images",
    ): List<ListMovieItemDto>
}
