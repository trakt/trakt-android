package tv.trakt.trakt.core.lists.sections.personal.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import java.time.Instant

internal interface ListsPersonalItemsLocalDataSource {
    suspend fun setItems(
        listId: TraktId,
        items: List<PersonalListItem>,
    )

    suspend fun getItems(listId: TraktId): List<PersonalListItem>

    suspend fun removeShows(
        listId: TraktId,
        showsIds: List<TraktId>,
        notify: Boolean,
    )

    suspend fun removeMovies(
        listId: TraktId,
        moviesIds: List<TraktId>,
        notify: Boolean,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
