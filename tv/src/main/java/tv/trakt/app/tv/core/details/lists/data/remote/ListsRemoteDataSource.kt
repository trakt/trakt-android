package tv.trakt.app.tv.core.details.lists.data.remote

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.ListMovieItemDto
import tv.trakt.app.tv.networking.openapi.ListShowItemDto

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
