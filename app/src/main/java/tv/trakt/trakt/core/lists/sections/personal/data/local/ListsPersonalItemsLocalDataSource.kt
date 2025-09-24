package tv.trakt.trakt.core.lists.sections.personal.data.local

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem

internal interface ListsPersonalItemsLocalDataSource {
    suspend fun addItems(
        listId: TraktId,
        items: List<PersonalListItem>,
    )

    suspend fun getItems(listId: TraktId): List<PersonalListItem>

    suspend fun removeShows(
        listId: TraktId,
        showsIds: List<TraktId>,
    )

    suspend fun removeMovies(
        listId: TraktId,
        moviesIds: List<TraktId>,
    )

    fun clear()
}
